/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
class JoyThread extends Thread implements JoyCallback {

    private JoyReader tr;
    private Motor mCompass;
    private Motor mHoehe;
    private String mqttPath;

    @Override
    public void run() {

        while (true) {
            tr = new JoyReader(mqttPath);

            tr.register(this);
            try {
                tr.doDemo();

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (tr.isConnected()) {
                try {
                    Thread.sleep(2000);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
            System.out.println("restart joy");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }

    }

    @Override
    public void setMotion(MqttMessage message) {
        try {
            System.out.println(message);
            byte[] bytes = message.getPayload();
            String sMsg = new String(bytes, StandardCharsets.UTF_8);
            JSONObject jo = new JSONObject(sMsg);
            int cmd = jo.getInt("cmd");
            System.out.println(cmd);
            mCompass.stop();
            mHoehe.stop();
            switch (cmd) {
                case 0:
                    mHoehe.hoch();
                    break;
                case 1:
                    mCompass.left();

                    break;
                case 2:
                    mCompass.right();
                    break;
                case 3:
                    mHoehe.runter();
                    break;
                default: {

                }

            }
            try {
                sleep(1500);//sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(JoyThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            mCompass.stop();
            mHoehe.stop();
            /*
             moCompass.stop();
             moHoehe.stop();
             moHoehe.hoch();
             */
        } catch (JSONException ex) {
            Logger.getLogger(JoyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void setMotors(Motor m1, Motor m2) {
        this.mCompass = m1;
        this.mHoehe = m2;
    }

    void setMQTTPath(String mqttPath) {
        this.mqttPath = mqttPath;
    }

}
