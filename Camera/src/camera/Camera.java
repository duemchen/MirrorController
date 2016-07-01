/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author duemchen
 */
public class Camera {

    private static final Logger log = Logger.getLogger(Camera.class);

    private MqttClient client;

    public Camera() throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        client = new MqttClient("tcp://duemchen.ddns.net:1883", "cam", persistence);
        client.setTimeToWait(5000);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("start compass.");
        Compass c = new Compass();
        c.start();
        System.out.println("start camera.");
        Camera cam = new Camera();
        cam.snapshot();
        // regelmässig zu festen Sekunden Foto schiessen. Thread.
        int phase;
        int lastPhase = -1;
        while (true) {
            Calendar cal = new GregorianCalendar();
            int stunde = cal.get(Calendar.HOUR_OF_DAY);
            int STUNDE_VON = 7;
            int STUNDE_BIS = 23;
            if ((stunde < STUNDE_VON) || (stunde > STUNDE_BIS)) {
                Thread.sleep(1000);
                continue;
            }
            // regelmässig Fotos
            int sekunde = cal.get(Calendar.SECOND);
            phase = sekunde / 10; // alle 10 sekunden wechsel  jede volle zehner wechselt
            //phase = phase % 4; // 4 phasen wenn ma unterteilen wollte

            if (phase == lastPhase) {
                Thread.sleep(300);
                continue;
            }
            lastPhase = phase;
            // zeit für ein Foto.
            cam.snapshot();

        }
    }

    private void snapshot() {
        //      System.out.println("shot...");
        try {
            Runtime rt = Runtime.getRuntime();
            Process p;

//            p = rt.exec("raspistill -n --nopreview -t 2000 -w 400 -h 300 -e jpg -o -");
            p = rt.exec("raspistill -n --nopreview -t 2000 -w 400 -h 300 -ex sports  -q 100 -e jpg -o -");

            Thread.sleep(8000); // sonst werden die bilder nicht sicher bereitgestellt
            byte[] currentImage = IOUtils.toByteArray(p.getInputStream());
//        System.out.println(currentImage.length);
            sendBild(currentImage);
            /*

             OutputStream out = null;

             try {
             out = new BufferedOutputStream(new FileOutputStream("/home/root/bilf.jpg"));
             out.write(currentImage);
             } finally {
             if (out != null) {
             out.close();
             }
             }
             */
            //client.disconnect();
            //client.close();
        } catch (Exception ex) {
            log.error(ex);
        }

    }

    void sendBild(byte[] bild) {
        //TODO Format json
        MqttMessage message = new MqttMessage();
        message.setQos(0);
        message.setPayload(bild);

        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/cam", message);
            //client.disconnect();
            //client.close();

        } catch (MqttException ex) {
            log.error(ex);
        }

    }

}
