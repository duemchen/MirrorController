/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import de.horatio.common.HoraIni;
import de.lichtmagnet.mavenmirrorcontroller.Regler.CMD;
import de.lichtmagnet.mavenmirrorcontroller.database.PosSpeicherThread;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
public class ReglerStart implements CompassCallback {

    public static Logger log = Logger.getLogger("ReglerLogger");

    private static SimpleDateFormat sdf = new SimpleDateFormat("mm");
    private static SimpleDateFormat sdfSec = new SimpleDateFormat("ss");

    private static int JOY_DELAY_IN_SECONDS = 30;
    CompassConnectorThread cct;
    PosSpeicherThread psp;
    MirrorList ml;

    private MqttClient client;

    private HashMap<String, Boolean> horizontal = new HashMap<String, Boolean>();
    private HashMap<String, Long> time = new HashMap<String, Long>();
    private static boolean stopped;
    private String MQTTLINK = "duemchen.feste-ip.net:56686";
    private long joyDelay;

    ReglerStart() {
        DOMConfigurator.configureAndWatch("log4j.xml", 5 * 1000);
        log.info("ReglerStart.START");
        MQTTLINK = HoraIni.LeseIniString(Regler.INIDATEI, "MQTT", "LINK_PORT", MQTTLINK, true);
        MQTTLINK = "tcp://" + MQTTLINK;
        ml = new MirrorList();
        ml.setMQTTLink(MQTTLINK);
        cct = new CompassConnectorThread();
        cct.register((CompassCallback) this);
        cct.setMQTTLink(MQTTLINK);
        cct.start();
        psp = new PosSpeicherThread();
        psp.setMqttLink(MQTTLINK);
        psp.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    log.info("Hook is working.............................................................");
                    cct.interrupt();
                    psp.interrupt();
                    cct.join(2000);
                    psp.join(2000);
                    log.info("Hook is happy ending........................................................");
                } catch (InterruptedException ex) {
                    log.error("ShutdownHook " + ex);
                }

                log.info("Hook says byebye");

            }
        });

        int last = -1;
        int lastsec = -1;
        stopped = false;
        while (!stopped) {
            try {
                // minuten
                int now = Integer.parseInt(sdf.format(new Date()));
                now = now / 15; // alle 15 min Wind openweather

                if (now != last) {
                    last = now;
                    OpenWeather ow = new OpenWeather();
                    double lon = 12.89;
                    double lat = 53.09;
                    ow.setCoord(lon, lat);
                    double wind = ow.getWind();
                    Regler.setWind(wind);
                    double cloud = ow.getCloud();
                    Regler.setCloud(cloud);
                    int windmax = HoraIni.LeseIniInt(Regler.INIDATEI, "WIND", "MAX", 6, true);
                    Regler.setWindMax(windmax);
                    log.info("wind: " + wind);
                }
                int nowsec = Integer.parseInt(sdfSec.format(new Date()));
                nowsec = nowsec / 15;
                if (nowsec != lastsec) {
                    sendZustand();
                }

            } catch (Exception ex) {
                log.error("while (!stopped):  " + ex);
            } finally {
                try {
                    Thread.sleep(1000);
                    // stopRegler(); //test fÃ¼r dienst
                } catch (InterruptedException ex) {
                    log.error("while (!stopped)2:  " + ex);
                }
            }
        }
        log.info("ReglerStart.ENDE");

    }

    private synchronized void sendCommand(String path, CMD cmd) {
        try {
            if (cmd == CMD.NEUTRAL) //TODO Format json
            {
                return;
            }

            JSONObject jo = new JSONObject();
            jo.put("cmd", cmd.ordinal());
            jo.put("source", 0); // der Regler sendet das Kommando selbst
            MqttMessage message = new MqttMessage();
            message.setPayload(jo.toString().getBytes());
            try {
                if (client == null) {
                    client = new MqttClient(MQTTLINK, "joyit");
                }
                if (client.isConnected()) {
                } else {
                    client.connect();
                }
                path = path.replaceFirst("compass", "joy");
                //client.publish("simago/joy", message);
                client.publish(path, message);
                
                //S ystem.out.println("sendCommand " + path + ":" + cmd);
                log.info("sendCommand " + path + ":" + cmd);
            } catch (MqttException ex) {

            }

        } catch (JSONException ex) {
        }

    }

    @Override
    public void setPosition(String path, String message) {
        ml.registerCompass(path, message);
        if (joyDelay > System.currentTimeMillis()) {
            log.info("Regler inaktiv für " + (joyDelay - System.currentTimeMillis()) / 1000 + " Sekunden");
            return;
        }
        if (path != null) {
            if (path.contains("simago/wind")) {
                JSONObject jw = new JSONObject(message);

                if (jw.getInt("time") == 10000) {
                    Regler.setWindLocal(jw.getInt("count"));
                }

                return;

            }

            if (path.contains("simago/joy")) {
                JSONObject jMess = new JSONObject(message);
                if (!jMess.has("source")) {
                    // nur der Regler sendet sich mit.
                    joyDelay = System.currentTimeMillis();
                    joyDelay += 1000 * HoraIni.LeseIniInt(Regler.INIDATEI, "JOY", "JOY_DELAY_IN_SECONDS", JOY_DELAY_IN_SECONDS, true);
                }
                return;  // nur registrieren, dass joy kommando kam.
            }

        }
        //compass Message
        if (!isTime(path)) {
            return;
        }

        log.info(path + "    " + message);
        // neue MACs bekanntgeben

        try {
            JSONObject position = new JSONObject(message);
            // fÃ¼r diese Message den richtigen Regler bauen. Steuersignal zurÃ¼ckbekommen und senden

            Regler r;
            r = new Regler(path, position, getHorizontal(path));
            sendCommand(r.getPath(), r.getCmd());

        } catch (Exception e) {
            log.error("setPosition: ", e);
            //e.printStackTrace();
        }
    }

    private boolean getHorizontal(String path) {
        // je spiegel abwechselnd horizon Verti
        Boolean b = horizontal.get(path);
        if (b == null) {
            b = false;
        }
        b = !b;
        horizontal.put(path, b);
        return b;
    }

    private boolean isTime(String path) {
        boolean result = false;
        Long nextTime = time.get(path);
        if (nextTime == null) {
            nextTime = new Long(0);
        }
        if (nextTime < System.currentTimeMillis()) {
            result = true;
            nextTime = System.currentTimeMillis() + 5000;
            time.put(path, nextTime);
        }
        return result;
    }

    public static void main(String[] args) throws InterruptedException {

    }

    static void stopRegler() {
        stopped = true;

    }

    private void sendZustand() {
        try {
            JSONObject jo = new JSONObject();
            jo.put("windLoc", Regler.getWindLocal());
            jo.put("windmax", Regler.getWindMax());
            jo.put("wind", Regler.getWind());
            jo.put("cloud", Regler.getCloud());
            //jo.put("mirror", Regler.getState());
            MqttMessage message = new MqttMessage();
            message.setRetained(true);
            message.setPayload(jo.toString().getBytes());
            try {
                if (client == null) {
                    client = new MqttClient(MQTTLINK, "joyit");
                }
                if (client.isConnected()) {
                } else {
                    client.connect();
                }
                String path = "simago/zustand";
                client.publish(path, message);
                log.info("sendZustand " + path + ":" + message);
            } catch (MqttException ex) {

            }

        } catch (JSONException ex) {
        }

    }

}
