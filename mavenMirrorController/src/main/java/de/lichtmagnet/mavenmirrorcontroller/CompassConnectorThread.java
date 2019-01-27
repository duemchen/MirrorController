/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author duemchen
 */
class CompassConnectorThread extends Thread implements CompassCallback {

    public static Logger log = Logger.getLogger("CompassConnectorThread");

    CompassCallback callback;
    private CompassReader tr;
    private boolean stop;
    private String MQTTLINK;

    public CompassConnectorThread() {
    }

    void register(CompassCallback x) {
        callback = x;
        callback.setPosition("info", "waiting for Compass");

    }

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            tr = new CompassReader();
            tr.setMQTTLink(MQTTLINK);
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
            log.error("restart");

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

    @Override
    public void setPosition(String path, String message) {
        callback.setPosition(path, message);
    }

    void setMQTTLink(String MQTTLINK) {
        this.MQTTLINK = MQTTLINK;
    }

}
