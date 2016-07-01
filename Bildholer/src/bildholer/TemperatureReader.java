/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bildholer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 *
 * @author duemchen
 */
public class TemperatureReader implements MqttCallback {

    private static Logger logVeranda = Logger.getLogger(TemperatureReader.class);
    private static Logger logHeizung = Logger.getLogger("HeizungLogger");

    MqttClient client;
    private String lastMessage = "";
    private boolean connectionOK;
    private boolean switchMessage = false;
    BildCallback callback;

    public TemperatureReader() {
        DOMConfigurator.configureAndWatch("log4j.xml", 5 * 1000);
    }

    public void register(BildCallback callback) {

        this.callback = callback;
    }

    public void doDemo() throws InterruptedException {
        Thread.sleep(1000);
        try {
            String tmpDir = "AAA";
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
            client = new MqttClient("tcp://duemchen.ddns.net:1883", "", dataStore);
            client.connect();
            client.setCallback(this);
            //client.subscribe("simago/veranda");
            client.subscribe("simago/heizung");
            //client.subscribe("simago/joy");

            MqttMessage message = new MqttMessage();
            message.setPayload("PahoDemo interessiert sich für simago/cam".getBytes());

            connectionOK = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectToMQTT() throws InterruptedException {
        Thread.sleep(1000);
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            // jeder client muss eine zufallsid generieren, um stress zu vermeiden
            SecureRandom random = new SecureRandom();
            String id = new BigInteger(60, random).toString(32);
            System.out.println("id=" + id);
            client = new MqttClient("tcp://duemchen.ddns.net:1883", id, persistence);
            client.connect();
            client.setCallback(this);
            client.subscribe("simago/test");
            client.subscribe("simago/veranda");

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
                System.out.println(message + " " + new Date());
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

    public static void main(String[] args) throws InterruptedException {
        TempReadThread instance = new TempReadThread();
        instance.setName("temperaturReader");
        instance.start();
        while (true) {
            Thread.sleep(1000);
            System.out.print(".");
        }
        //new TemperatureReader().doDemo();
    }
}
