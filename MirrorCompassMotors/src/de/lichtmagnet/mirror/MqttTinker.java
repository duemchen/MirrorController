/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
class MqttTinker {

    private MqttClient client;
    private static MqttTinker instance = null;

    public static MqttTinker getInstance() {
        if (instance == null) {
            try {
                instance = new MqttTinker();
            } catch (MqttException ex) {
                Logger.getLogger(MqttTinker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }

    public MqttTinker() throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        SecureRandom random = new SecureRandom();
        String id = new BigInteger(60, random).toString(32);
        System.out.println("id=" + id);
        client = new MqttClient("tcp://duemchen.ddns.net:1883", id, persistence);

    }

    void sendTemps(double iCompass, double iPitch, double iRoll, double sCompass, double sPitch, double sRoll, short calibration) throws JSONException {
        //TODO Format json

        JSONObject jo = new JSONObject();
        JSONObject positions = new JSONObject();
        JSONObject soll = new JSONObject();
        JSONObject system = new JSONObject();

        //
        positions.put("compass", iCompass);
        positions.put("pitch", iPitch);
        positions.put("roll", iRoll);
        //
        soll.put("compass", sCompass);
        soll.put("height", sPitch);
        //
        system.put("calibration", calibration);
        //
        jo.put("positions", positions);
        jo.put("soll", soll);
        jo.put("system", system);

        MqttMessage message = new MqttMessage();
        Runtime rt = Runtime.getRuntime();

        String content = jo.toString();
        message.setPayload(content.getBytes());
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/tinker", message);
        } catch (MqttException ex) {
            Logger.getLogger(MqttTinker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void sendByebye() {
        //TODO Format json
        MqttMessage message = new MqttMessage();
        String content = "Byebye vom Tinker.  Speicher:" + getMemory();
        message.setPayload(content.getBytes());
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/tinker", message);

        } catch (MqttException ex) {
            Logger.getLogger(MqttTinker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void sendHallo() {
        MqttMessage message = new MqttMessage();
        String content = "Hallo vom Heizungsregler. Speicher: " + getMemory();
        message.setPayload(content.getBytes());

        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/tinker", message);
        } catch (MqttException ex) {
            Logger.getLogger(MqttTinker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String getMemory() {
        Runtime rt = Runtime.getRuntime();
        return new DecimalFormat("###,###.###").format((rt.totalMemory() - rt.freeMemory()));

    }

}
