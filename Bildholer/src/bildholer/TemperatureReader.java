/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bildholer;

import de.horatio.common.HoraIni;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author duemchen
 */
public class TemperatureReader implements MqttCallback {

    private static Logger logVeranda = Logger.getLogger(TemperatureReader.class);
    private static Logger logHeizung = Logger.getLogger("HeizungLogger");
    private static Logger logElektro = Logger.getLogger("ElektroLogger");
    private static boolean stop;

    MqttClient client;
    private String lastMessage = "";
    private boolean connectionOK;
    private boolean switchMessage = false;
    BildCallback callback;
    private final String INI = "bildholer.ini";
    private String MQTTLINK = "duemchen.feste-ip.net:56686";

    public TemperatureReader() {
        DOMConfigurator.configureAndWatch("log4j.xml", 5 * 1000);
    }

    public void register(BildCallback callback) {

        this.callback = callback;
    }

    public void connectToMQTT() throws InterruptedException {
        Thread.sleep(1000);
        try {
            //
            MQTTLINK = HoraIni.LeseIniString(INI, "MQTT", "LINK_PORT", MQTTLINK, true);
            MQTTLINK = "tcp://" + MQTTLINK;
            //
            MemoryPersistence persistence = new MemoryPersistence();
            // jeder client muss eine zufallsid generieren, um stress zu vermeiden
            SecureRandom random = new SecureRandom();
            String id = new BigInteger(60, random).toString(32);
            System.out.println("id=" + id);
            client = new MqttClient(MQTTLINK, id, persistence);
            client.connect();
            client.setCallback(this);
            client.subscribe("simago/test");
            client.subscribe("simago/veranda");
            client.subscribe("simago/elektro");
            // client.subscribe("simago/compass/+");
            // http://mosquitto.org/man/mqtt-7.html  + nur die macs, # auch die root
            connectionOK = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        connectionOK = false;
        System.out.println("lost");

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if ("simago/test".equals(topic)) {
            logHeizung.info("" + message);
        } else {
            if ("simago/veranda".equals(topic)) {
                logVeranda.info("" + message);

            } else {
                if ("simago/elektro".equals(topic)) {
                    logElektro.info("" + message);
                    //System.out.println(message + " " + new Date());

                } else {

                    System.out.println("else: " + topic);
                    //eintragen aller neuen Compasse zur freischaltung, onlineanzeige, auswahl, Halten in hashmap., ini
                    //System.out.println(topic + ", " + message + " " + new Date());
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token
    ) {
        System.out.println("delivery");
    }

    public String getLastMessage() {

        return lastMessage;
    }

    public boolean isConnected() {

        return connectionOK;

    }

    private static void start(String[] args) {
        System.out.print("Start Bildholer.");

        TempReadThread instance = new TempReadThread();
        instance.setName("temperaturReader");
        instance.start();
        stop = false;
        while (!stop) {
            try {
                Thread.sleep(1000);
                //System.out.print(".");
            } catch (InterruptedException ex) {
                stop = true;
            }
        }
        System.out.print("Beende Bildholer....");
        instance.doStop();
        instance.interrupt();

    }

    private static void stop(String[] args) {
        System.out.print("Stop Bildholer.");
        stop = true;
    }

    public static void main(String[] args) {

        if (args.length > 0) {
            if ("start".equals(args[0])) {
                start(args);
            } else {
                if ("stop".equals(args[0])) {
                    stop(args);
                } else {
                    System.out.print("Parameter start oder stop notwendig.");
                }
            }
        } else {
            System.out.print("Parameter start oder stop notwendig.");
            start(args);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(TemperatureReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.print("stop notwendig.");
            String[] s = new String[1];
            stop(s);

        }
    }
}
