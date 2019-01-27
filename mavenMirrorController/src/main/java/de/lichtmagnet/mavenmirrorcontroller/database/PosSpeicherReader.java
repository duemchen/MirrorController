/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller.database;

import static de.lichtmagnet.mavenmirrorcontroller.ReglerStart.log;
import java.math.BigInteger;
import java.security.SecureRandom;
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
class PosSpeicherReader implements MqttCallback {

    PosSpeicherCallback callback;
    MqttClient client;
    private boolean connectionOK;
    private String MQTTLINK;

    public PosSpeicherReader() {
    }

    void register(PosSpeicherCallback x) {
        callback = x;
        callback.setPosition("info", "registriert.");

    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        connectionOK = false;
        log.error("connectionLost:" + thrwbl);
    }

    @Override
    public void messageArrived(String path, MqttMessage mm) throws Exception {
        if (mm != null) {
            byte[] b = mm.getPayload();
            callback.setPosition(path, new String(b));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        log.info("deliveryComplete");
    }

    public void connectToMQTT() throws InterruptedException {
        Thread.sleep(1000);
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            SecureRandom random = new SecureRandom();
            String id = new BigInteger(60, random).toString(32);
            client = new MqttClient(MQTTLINK, "id" + id, persistence);
            client.connect();
            client.setCallback(this);
            // client.subscribe("simago/compass");
            //client.subscribe("simago/compass/74-DA-38-3E-E8-3C");
            client.subscribe("simago/save/#");
            connectionOK = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    boolean isConnected() {
        return connectionOK;
    }

    void setMqttLink(String MQTTLINK) {
        this.MQTTLINK = MQTTLINK;
    }
}
