/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx1;

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
class CompassReader implements MqttCallback {

    CompassCallback callback;
    private MqttClient client;
    private boolean connectionOK;

    public CompassReader() {
    }

    void register(CompassCallback x) {
        callback = x;
        callback.setPosition("registriert.");

    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        connectionOK = false;
        System.out.println("clost:" + thrwbl);
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        if (mm == null) {
            System.out.println("cMessage NULL");
        } else {
            callback.setPosition(mm.getPayload().toString());
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        System.out.println("c delivery");
    }

    public void connectToMQTT() throws InterruptedException {
        Thread.sleep(1000);
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient("tcp://duemchen.ddns.net:1883", "compass", persistence);
            client.connect();
            client.setCallback(this);
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
