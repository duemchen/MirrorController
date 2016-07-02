/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 *
 * compass als eigener thread, der Position liest Mit jedem Bild mitsendet. ist
 * ok. bzw. extra sendet ??? wozu? man stellt die pos ein. Wenn sie richtig ist,
 * merkt man sich die in der datenbank (ziel,datumuhrzeit,dir,pitch,roll) dazu
 * eine Anfrage vom server getPosition() savePosition() Endversion: Jede Minute
 * bekommt der Spiegel eine neue Sollposition und regelt sich hin
 * EntwicklVersion: getpos() und cmd hoch rechts...
 *
 * thread kümmert sich um positionsdaten function zum abholen oder ereignis
 * senden wenn neue position mittelwertbildung mqtt sender
 *
 */
public class Compass extends Thread {

    private static final Logger log = Logger.getLogger(Compass.class);
    private MqttClient client;
    private String mqttPath;

    Compass(String mqttPath) {
        this.mqttPath = mqttPath;
        System.out.println("start compass. Path: " + mqttPath);
    }

    void sendCommand(int dir, int pitch, int roll) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("dir", dir);
            jo.put("pitch", pitch);
            jo.put("roll", roll);
            MqttMessage message = new MqttMessage();
            message.setPayload(jo.toString().getBytes());
            try {
                if (client == null) {
                    client = new MqttClient("tcp://duemchen.ddns.net:1883", "compass");
                }
                if (!client.isConnected()) {
                    client.connect();
                }
                client.publish("simago/" + mqttPath, message);

            } catch (MqttException ex) {
                log.error(ex);

            }

        } catch (JSONException ex) {
            log.debug(ex);
        }

    }

    private static int toGrad(int read) {
        int result = read;
        if (result > 127) {
            result -= 256;
        }
        return result;
    }

    public void Compass() {

    }

    @Override
    public void run() {
        I2CBus bus;
        int deviceNr = 0x60;
        try {
            // create I2C communications bus instance
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            I2CDevice device = bus.getDevice(deviceNr);
            while (true) {
                try {
                    int b1 = device.read(2);
                    int b2 = device.read(3);
                    int dir = ((b1 << 8) + b2) / 10;

                    int pitch = toGrad(device.read(4));
                    int roll = toGrad(device.read(5));

                    //System.out.println("   dir:" + dir + "  p:" + pitch + "   r:" + roll);
                    sendCommand(dir, pitch, roll);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    Thread.sleep(2000);
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Compass run sagt Tschüss");

    }

}
