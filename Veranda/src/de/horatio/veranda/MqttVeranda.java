/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.horatio.veranda;

import de.horatio.common.HoraTime;
import java.text.DecimalFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
class MqttVeranda {

    private static final Logger log = Logger.getLogger(MqttVeranda.class);

    private final MqttClient client;
    private static MqttVeranda instance = null;

    public static MqttVeranda getInstance() {
        if (instance == null) {
            try {
                instance = new MqttVeranda();
            } catch (MqttException ex) {
                log.error(ex);
            }
        }
        return instance;
    }

    public MqttVeranda() throws MqttException {
        client = new MqttClient("tcp://duemchen.ddns.net:1883", "publishTempVeranda");

    }

    void sendTemps() throws JSONException {
        //TODO Format json

        JSONObject jo = new JSONObject();
        JSONObject temperaturen = new JSONObject();
        JSONObject relais = new JSONObject();
        for (Veranda.TEMP temp : Veranda.TEMP.values()) {
            //content += temp.name() + "=" + temp.getTempLast() + ", ";
            temperaturen.put(temp.name(), temp.getTempLast());
        }

        for (Veranda.DA da : Veranda.DA.values()) {
            relais.put(da.name(), da.isOn());
        }
        jo.put("time", HoraTime.dateToStr(new Date()));
        jo.put("temperatures", temperaturen);
        jo.put("relais", relais);

        MqttMessage message = new MqttMessage();
        Runtime rt = Runtime.getRuntime();

        String content = jo.toString();
        message.setPayload(content.getBytes());
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/veranda", message);
        } catch (MqttException ex) {
            log.error(ex);
        }

    }

    void sendByebye() {
        //TODO Format json
        MqttMessage message = new MqttMessage();
        String content = "Byebye vom FussbodenHeizungsregler.  Speicher:" + getMemory();
        message.setPayload(content.getBytes());
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/veranda", message);

        } catch (MqttException ex) {
            log.error(ex);
        }

    }

    void sendHallo() {
        MqttMessage message = new MqttMessage();
        String content = "Hallo vom FussbodenHeizungsregler. Speicher: " + getMemory();
        message.setPayload(content.getBytes());

        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/veranda", message);
        } catch (MqttException ex) {
            log.error(ex);
        }

    }

    private String getMemory() {
        Runtime rt = Runtime.getRuntime();
        return new DecimalFormat("###,###.###").format((rt.totalMemory() - rt.freeMemory()));

    }

}
