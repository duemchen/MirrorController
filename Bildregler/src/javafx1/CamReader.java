/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx1;

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
class CamReader implements MqttCallback {

    CamCallback callback;
    CompassCallback coCallback;
    private MqttClient client;
    private boolean connectionOK;
    private final String INIDATEI = "CAMWORKER.INI";

    public CamReader() {

    }

    void register(CamCallback x) {
        callback = x;
        callback.setBild(null);
    }

    void register(CompassCallback x) {
        coCallback = x;
        coCallback.setPosition("register");
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        connectionOK = false;
        System.out.println("lost:" + thrwbl);
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        // System.out.println(string);
        if (mm == null) {
            System.out.println("Message NULL");
        } else {
            if ("simago/camera".equals(string)) {
                callback.setBild(mm.getPayload());
            }
            if ("simago/cam".equals(string)) {
                callback.setBild(mm.getPayload());
            }
            if ("simago/panorama".equals(string)) {
                callback.setBild(mm.getPayload());
            }

            if ("simago/compass".equals(string)) {
                byte[] pos = mm.getPayload();
                coCallback.setPosition(new String(pos));
            }
            if (string.contains("simago/compass")) {
                byte[] pos = mm.getPayload();
                coCallback.setPosition(new String(pos));
            }

        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        System.out.println("delivery");
    }

    public void connectToMQTT(String MQTTLINK) throws InterruptedException {
        Thread.sleep(1000);
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            // jeder client muss eine zufallsid generieren, um stress zu vermeiden
            SecureRandom random = new SecureRandom();
            String id = new BigInteger(60, random).toString(32);
            System.out.println("id=" + id);
            client = new MqttClient(MQTTLINK, id, persistence);
            client.connect();
            client.setCallback(this);
            // client.subscribe("simago/cam");
            client.subscribe("simago/camera");
            // client.subscribe("simago/panorama");
            //client.subscribe("simago/compass");
            client.subscribe("simago/compass/74-DA-38-3E-E8-3C");
            connectionOK = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    boolean isConnected() {
        return connectionOK;
    }
}
