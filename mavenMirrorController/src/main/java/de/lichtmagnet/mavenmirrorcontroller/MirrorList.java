/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
class MirrorList {

    private final String PATH = "simago/mirrors";
    private String MQTTLINK;
    private final HashMap<String, MirrorState> list;
    private MqttClient client;

    MirrorList() {
        this.list = new HashMap<String, MirrorState>();
        // aus ini laden.
    }

    void setMQTTLink(String MQTTLINK) {
        this.MQTTLINK = MQTTLINK;
    }

    void registerCompass(String path, String message) {

        if (path == null) {
            return;
        }
        if (!path.contains("compass")) {
            return;
        }

        //MAC schon da?
        int i = path.lastIndexOf("/");
        String mac = path.substring(i + 1);
        MirrorState ms = list.get(mac);
        if (ms == null) {
            ms = new MirrorState(mac);
            list.put(mac, ms);
            //liste abspeichern.
            sendList();
        }
        ms.setTime(new Date()); // letztes CompasSignal. Daraus kann grün/grau werden auf Handy. Alle/Online
    }

    public String toString() {
        String s = "";
        for (String key : list.keySet()) {
            s += "\n";
            s += list.get(key);

        }
        return "list:\n" + s;
    }

    private synchronized void sendList() {
        try {
            JSONArray ja = new JSONArray();
            String s = "";
            for (String key : list.keySet()) {
                JSONObject o = list.get(key).getJson();
                ja.put(o);
            }

            JSONObject jo = new JSONObject();
            jo.put("inhalt", "MirrorList");
            jo.put("liste", ja);
            MqttMessage message = new MqttMessage();
            message.setPayload(jo.toString().getBytes());
            message.setRetained(true);
            try {
                if (client == null) {
                    MemoryPersistence persistence = new MemoryPersistence();
                    SecureRandom random = new SecureRandom();
                    String id = new BigInteger(60, random).toString(32);

                    client = new MqttClient(MQTTLINK, id, persistence);
                }
                if (client.isConnected()) {
                } else {
                    client.connect();
                }
                //client.publish("simago/joy", message);
                client.publish(PATH, message);

            } catch (MqttException ex) {

            }

        } catch (JSONException ex) {
        }

    }

}
