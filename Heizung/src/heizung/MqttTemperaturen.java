/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heizung;

import de.horatio.common.HoraTime;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
class MqttTemperaturen {

    private MqttClient client;
    private static MqttTemperaturen instance = null;

    public static MqttTemperaturen getInstance() {
        if (instance == null) {
            try {
                instance = new MqttTemperaturen();
            } catch (MqttException ex) {
                Logger.getLogger(MqttTemperaturen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }

    public MqttTemperaturen() throws MqttException {
        client = new MqttClient("tcp://duemchen.ddns.net:1883", "publishTemperatures");

    }

    void sendTemps() throws JSONException {
        //TODO Format json
      
        JSONObject jo = new JSONObject();
        JSONObject temperaturen = new JSONObject();
        JSONObject relais= new JSONObject();
        for (Heizung.TEMP temp : Heizung.TEMP.values()) {
            //content += temp.name() + "=" + temp.getTempLast() + ", ";
           temperaturen.put(temp.name(),temp.getTempLast());
        }
        
        for (Heizung.DA da: Heizung.DA.values()){
            relais.put(da.name(),da.isOn());            
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
            client.publish("simago/test", message);
        } catch (MqttException ex) {
            Logger.getLogger(MqttTemperaturen.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void sendByebye() {
        //TODO Format json
        MqttMessage message = new MqttMessage();
        String content = "Byebye vom Heizungsregler.  Speicher:" + getMemory();
        message.setPayload(content.getBytes());
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/test", message);

        } catch (MqttException ex) {
            Logger.getLogger(MqttTemperaturen.class.getName()).log(Level.SEVERE, null, ex);
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
            client.publish("simago/test", message);
        } catch (MqttException ex) {
            Logger.getLogger(MqttTemperaturen.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String getMemory() {
        Runtime rt = Runtime.getRuntime();
        return new DecimalFormat("###,###.###").format((rt.totalMemory() - rt.freeMemory()));

    }

}
