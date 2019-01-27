/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

import de.horatio.common.HoraFile;
import de.horatio.common.HoraIni;
import de.lichtmagnet.mirror.Constanten.DA;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duemchen
 */
public class Mirror {

    //
    final static String datei = "mirror.ini";
    String mac;
    JoyThread joy;
    private String MQTTLINK = "duemchen.feste-ip.net:56686";

    /**
     * iii
     */
    public Mirror() throws IOException, InterruptedException, Exception {
        System.out.println("ini: " + HoraFile.getCanonicalPath(datei));
        MQTTLINK = HoraIni.LeseIniString(datei, "MQTT", "LINK", MQTTLINK, true);
        MQTTLINK = "tcp://" + MQTTLINK;
        this.mac = TestMac.getMacAddress(mac);
        System.out.println("Mirror Mac: " + mac);
        Motor m1 = new Motor(DA.L1, DA.R1, DA.E1);
        Motor m2 = new Motor(DA.L2, DA.R2, DA.E2);
        //Empfänger für die Steuerbefehle
        joy = new JoyThread();
        joy.setName("joystick");
        joy.setMotors(m1, m2);
        joy.setMQTTPath(mac);
        joy.setMqqtLink(MQTTLINK);
        joy.start();
        // Testlauf Motoren
        m1.toWest();
        Thread.sleep(2000);
        m1.toEast();
        Thread.sleep(2000);
        m1.stop();
        m2.hoch();
        Thread.sleep(2000);
        m2.runter();
        Thread.sleep(2000);
        m2.stop();
        //
        Compass c = new Compass(mac);
        c.setMqttLink(MQTTLINK);
        c.start();
//
        //
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Mirror.class.getName()).log(Level.SEVERE, null, ex);
                }

                for (DA da : DA.values()) {
                    da.off();
                }

            }
        });
        int i = 0;
        while (true) {
            if (true) {
                Thread.sleep(7000);
                continue;
            }
            System.out.println(System.currentTimeMillis());
//            if (DA.LIVE.isOn()) {
//                DA.LIVE.off();
//            } else {
//                DA.LIVE.on();
//            }
            if (i % 2 == 0) {
                try {
                    //TODO minütliche info ans MQTT, nach minutenwechsel

                } catch (Exception ex) {
                    Logger.getLogger(Mirror.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            i++;

        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException, Exception {
        System.out.println("Hi.");

        Mirror m = new Mirror();

    }

}
