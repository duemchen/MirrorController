/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import de.lichtmagnet.mavenopenweather.OpenWeather;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
public class Start implements CompassCallback {

    private static SimpleDateFormat sdf = new SimpleDateFormat("HH");
    CompassConnectorThread cct;
    Regler regler;

    private Start() {
        regler = new Regler();
        cct = new CompassConnectorThread();
        cct.register((CompassCallback) this);
        cct.start();

        int last = 0;
        while (true) {
            try {
                int now = Integer.parseInt(sdf.format(new Date()));
                if (now != last) {
                    last = now;
                    OpenWeather ow = new OpenWeather();
                    double lon = 12.89;
                    double lat = 53.09;
                    ow.setCoord(lon, lat);
                    double wind = ow.getWind();
                    regler.setWind(wind);

                }

            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start Spiegelsteuerung");
        Start start = new Start();

    }

    @Override
    public void setPosition(String s) {
        //System.out.println(new Date() + " pos:" + s);
        try {
            JSONObject position = new JSONObject(s);
            regler.setIstPosition(position);

        } catch (Exception e) {
            System.out.println(s);
        }
    }
}
