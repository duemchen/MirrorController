/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webcamrequest;

import de.horatio.common.HoraFile;
import de.horatio.common.HoraIni;
import de.horatio.common.HoraTime;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
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
public class WebCamRequest {

    private static final Logger log = Logger.getLogger(WebCamRequest.class);
    private static final String LOG4J = "log4j.xml";
    //
    private MqttClient client;

    private final String INI = "camera.ini";
    //private String MQTTLINK = "duemchen.feste-ip.net:56686";
    private String MQTTLINK = "192.168.10.51:1883";
    //camera
    private String webPage = "http://192.168.10.41/auto.jpg";
    private String name = "admin";
    private String password = "olfi";

    public WebCamRequest() throws MqttException, SocketException {
        System.out.println("Log4J: " + HoraFile.getCanonicalPath(LOG4J));
        System.out.println("Ini:   " + HoraFile.getCanonicalPath(INI));

        MemoryPersistence persistence = new MemoryPersistence();
        SecureRandom random = new SecureRandom();
        String id = new BigInteger(60, random).toString(32);
        System.out.println("camid=" + id);
        MQTTLINK = HoraIni.LeseIniString(INI, "MQTT", "LINK_PORT", MQTTLINK, true);
        String link = "tcp://" + MQTTLINK;
        client = new MqttClient(link, id, persistence);
        client.setTimeToWait(5000);
    }

    public double getWind() throws MalformedURLException, IOException {
        System.out.println("*** START **");
        try {

            String authString = name + ":" + password;
            System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            System.out.println("Base64 encoded auth string: " + authStringEnc);

            URL url = new URL(webPage);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();
            // volständiges bild
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            sendBild(byteArray);
            // so bild oft unvollständig
//            byte[] buffer = new byte[is.available()];
//            is.read(buffer);
//            sendBild(buffer);
//            File targetFile = new File("d:/bild.jpg");
//            OutputStream outStream = new FileOutputStream(targetFile);
//            outStream.write(buffer);
            System.out.println("*** END ***");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;

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
            log.info("Bild senden...");
            if (!client.isConnected()) {
                client.connect();
            }
            client.publish("simago/camera", message);
            System.out.println("Bild gesendet " + HoraTime.dateToStr(new Date()));
            log.info("Bild gesendet " + HoraTime.dateToStr(new Date()));
            //client.publish("simago/cam/" + mac, message);
            //client.disconnect();
            //client.close();

        } catch (MqttException ex) {
            log.error(ex);
        }

    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, MqttException, InterruptedException {
        DOMConfigurator.configureAndWatch(LOG4J, 2 * 1000);
        WebCamRequest wc = new WebCamRequest();
        while (true) {
            Thread.sleep(5000);
            wc.getWind();
        }
    }

}
