/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller.database;

import de.horatio.common.HoraFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
public class PosSpeicherThread extends Thread implements PosSpeicherCallback {

    public static Logger log = Logger.getLogger("PositionsSpeicher");
    private final String INI = "save.ini";
    private final SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");//13.11.2016 10:10:08
    //private PosSpeicherCallback callback;
    private PosSpeicherReader tr;
    private boolean stop;
    private String MQTTLINK;

    public PosSpeicherThread() {
    }

//    void register(PosSpeicherCallback x) {
//        callback = x;
//        callback.setPosition("info", "waiting for input");
//
//    }
    @Override
    public void run() {
        stop = false;
        while (!stop) {
            tr = new PosSpeicherReader();
            tr.setMqttLink(MQTTLINK);

            tr.register(this);
            try {
                tr.connectToMQTT();

            } catch (InterruptedException e1) {
                stop = true;

            }
            while (tr.isConnected()) {
                if (stop) {
                    break;
                }
                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    stop = true;

                }
            }
            log.info("restart");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                stop = true;

            }

        }
        //beenden
        try {
            tr.client.disconnect(2000);
        } catch (MqttException ex) {

        }
        try {
            tr.client.close();
        } catch (MqttException ex) {

        }

    }

    /**
     * Speichert je nach topic in bestimmte datei Das kommt an:
     * {"roll":10,"dir":323,"pitch":-12,"cmd":"save","topic":"simago/compass"}
     */
    @Override
    public void setPosition(String topic, String message) {
        try {
            JSONObject r = new JSONObject(message);
            //Datum/Uhrzeit ergänzen
            r.put("time", sd.format(new Date()));
            String messTopic = r.getString("topic");//simago/compass/74-DA-38-3E-E8-3C
            int i = messTopic.lastIndexOf("/");
            //nur die MAC
            messTopic = messTopic.substring(i + 1);
            String datei = messTopic + ".txt";
            log.info(r.toString());
            HoraFile.fileAppend(datei, r.toString());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void setMqttLink(String MQTTLINK) {
        this.MQTTLINK = MQTTLINK;
    }

}
