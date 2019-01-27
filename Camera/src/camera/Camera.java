/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;

import de.horatio.common.HoraFile;
import de.horatio.common.HoraIni;
import java.math.BigInteger;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
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
    final String LOG4J = "log4j.xml";
    final String INI = "camera.ini";
    private MqttClient client;
    private String mac;
    private String MQTTLINK = "duemchen.feste-ip.net:56686";

    public Camera() throws MqttException, SocketException {
        System.out.println("Log4J: " + HoraFile.getCanonicalPath(LOG4J));
        System.out.println("Ini:   " + HoraFile.getCanonicalPath(INI));
        DOMConfigurator.configureAndWatch(LOG4J, 10 * 1000);
        MQTTLINK = HoraIni.LeseIniString(INI, "MQTT", "LINK_PORT", MQTTLINK, true);
        MQTTLINK = "tcp://" + MQTTLINK;
        //
        this.mac = TestMac.getMacAddress("wlan");
        MemoryPersistence persistence = new MemoryPersistence();
        SecureRandom random = new SecureRandom();
        String id = new BigInteger(60, random).toString(32);
        System.out.println("camid=" + id);
        client = new MqttClient(MQTTLINK, id, persistence);
        client.setTimeToWait(5000);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("start compass.");
        //   Compass c = new Compass();
        //   c.start();
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
                //    Thread.sleep(300);
                //      continue;
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

            Thread.sleep(2000); // sonst werden die bilder nicht sicher bereitgestellt
            byte[] currentImage = IOUtils.toByteArray(p.getInputStream());
            //System.out.println(currentImage.length + "   " + new Date());
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

        String base64String = Base64.encodeBase64String(bild);
        byte[] backToBytes = Base64.decodeBase64(base64String);

        //  System.out.println("bild.length: " + bild.length);
        //  System.out.println("base64String.length: " + base64String.length());
        MqttMessage message = new MqttMessage();
        message.setQos(0);
        //message.setPayload(bild);
        message.setPayload(base64String.getBytes(StandardCharsets.UTF_8));

        try {
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/camera", message);
            // System.out.println("gesendet");
            //client.publish("simago/cam/" + mac, message);
            //client.disconnect();
            //client.close();

        } catch (MqttException ex) {
            log.error(ex);
        }

    }

}
