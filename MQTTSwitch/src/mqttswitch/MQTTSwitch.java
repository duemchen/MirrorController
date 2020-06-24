/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mqttswitch;

import de.horatio.common.HoraIni;
import de.horatio.common.HoraTime;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author duemchen
 */
public class MQTTSwitch implements MqttCallback {

    /**
     *
     * @author duemchen
     */
//    private static Logger logHeizung = Logger.getLogger("HeizungLogger");
    private static boolean stop;

    MqttAsyncClient client;
    private String lastMessage = "";
    private boolean connectionOK;
    private boolean switchMessage = false;
    SwitchCallback callback;
    private final String INI = "mqttswitch.ini";
    private String MQTTLINK = "duemchen.feste-ip.net:56686";

    public MQTTSwitch() {
       // DOMConfigurator.configureAndWatch("log4j.xml", 5 * 1000);
    }

    public void register(SwitchCallback callback) {

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
            client = new MqttAsyncClient(MQTTLINK, id, persistence);
            client.connect();
            Thread.sleep(1000);
            client.setCallback(this);
            client.subscribe("home/OpenMQTTGateway_ESP32_ALL/433toMQTT/#", 0);
//            client.subscribe("sonnen/#");
//            client.subscribe("simago/test");
//            client.subscribe("simago/veranda");
//            client.subscribe("simago/elektro");
            // client.subscribe("simago/compass/+");
            // http://mosquitto.org/man/mqtt-7.html  + nur die macs, # auch die root
            connectionOK = true;
            System.out.println("connected.");
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
        if ("home/OpenMQTTGateway_ESP32_ALL/433toMQTT".equals(topic)) {
            //System.out.println(topic + ", |||||" + message + "|||||| " + new Date());
            try {
                byte[] bb = message.getPayload();
                String s = new String(bb);
                int i = Integer.parseInt(s);
                System.out.println("433: " + i);
                String sd = "" + HoraTime.dateOnlyToStr(new Date(),"dd.MM.yyyy HH:mm:ss.SSS");
                MqttMessage echo;
                switch (i) {
                    case 9803110:
                        echo = new MqttMessage(sd.getBytes());
                        client.publish("433/teich", echo);
                        break;
                    case 1228814:
                        echo = new MqttMessage(sd.getBytes());
                        client.publish("433/briefkasten", echo);
                        break;
                    case 14222350:
                        echo = new MqttMessage(sd.getBytes());
                        client.publish("433/oben", echo);
                        break;
                    case 1115150:
                        echo = new MqttMessage(sd.getBytes());
                        client.publish("433/unten", echo);
                        break;
                        
                    default:
                        //legt die Zahl einfach an.
                        echo = new MqttMessage(sd.getBytes());
                        client.publish("433/"+i, echo);

                }

               // logHeizung.info("" + message);
            } catch (Exception e) {
                //System.out.println(e);
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
        System.out.print("Start MQTTSwitch.");

        SwitchReadThread instance = new SwitchReadThread();
        instance.setName("switchReader");
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
        System.out.print("Beende MQTTSwitch....");
        instance.doStop();
        instance.interrupt();

    }

    private static void stop(String[] args) {
        System.out.print("Stop MQTTSwitch.");
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
                java.util.logging.Logger.getLogger(MQTTSwitch.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.print("stop notwendig.");
            String[] s = new String[1];
            stop(s);

        }
    }
}
