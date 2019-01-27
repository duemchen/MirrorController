/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author duemchen
 */
class JoyReader implements MqttCallback {

    private static Logger log = Logger.getLogger(JoyReader.class);

    private JoyCallback callback;
    private MqttClient client;
    private boolean connectionOK;
    private final String mqttPath;
    private String MQTTLINK;

    JoyReader(String mqttPath) {
        this.mqttPath = mqttPath;
    }

    public void register(JoyCallback callback) {

        this.callback = callback;
    }

    public void doDemo() throws InterruptedException {
        Thread.sleep(1000);
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            SecureRandom random = new SecureRandom();
            String id = new BigInteger(60, random).toString(32);
            System.out.println("id=" + id);
            client = new MqttClient(MQTTLINK, id, persistence);
            client.connect();
            client.setCallback(this);
            client.subscribe("simago/joy/" + mqttPath);
            connectionOK = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        connectionOK = false;
        System.out.println("lostJoy " + new Date());

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
      //  System.out.println("topic:" + topic);
        //  System.out.println("msg:" + message);
        callback.setMotion(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("delivery joy");
    }

    public boolean isConnected() {

        return connectionOK;

    }

    void setMqttLink(String MQTTLINK) {
        this.MQTTLINK = MQTTLINK;
    }

}
