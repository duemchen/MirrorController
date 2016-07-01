/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
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
 *
 * Regelung Empfang der Position Bewegung hoch oder dir im Wechsel Nur alle 5
 * Sek.
 *
 */
public class Regler {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private double wind = 0;

    private Point getSollPointSunCalc(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        Point result = new Point();
        double x = Messpunkt.getSollAzimuth(cal);
        double y = Messpunkt.getSollLatitude(cal);

        x = (int) (Math.round(x));
        y = (int) (Math.round(y));
        result.setLocation(x, y);
        return result;
    }

    private boolean isOld(JSONObject js) {
        Messpunkt mp = new Messpunkt(js);
        Calendar akt = mp.getZeitpunkt();
        Calendar now = new GregorianCalendar();
        now.setTime(new Date());
        Calendar diff = new GregorianCalendar();
        diff.setTimeInMillis(now.getTimeInMillis() - akt.getTimeInMillis());
        int tage = diff.get(Calendar.DAY_OF_YEAR);
        // System.out.println("tage:" + tage);
        try {

            Date stichtag = sdf.parse("25.05.2016");
            return (!stichtag.before(akt.getTime()));

        } catch (ParseException ex) {
            Logger.getLogger(Regler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tage > 14;

    }

    void setWind(double wind) {
        this.wind = wind;
    }

    // hoch links rechts runter 0,1,2,3
    enum CMD {

        HOCH, LINKS, RECHTS, RUNTER
    }

    private static SimpleDateFormat uhrzeitFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat hhFormat = new SimpleDateFormat("HH");
    private String datei = "logfile.txt";  // die handgesammelten Sollpositionen
    private JSONObject position;
    private MqttClient client;
    private boolean nowDir;
    private long nexttime = 0;

    private ArrayList<JSONObject> loadListe(String listePoints) throws FileNotFoundException, IOException {
        // Arraylist erstellen
        final ArrayList<JSONObject> result = new ArrayList<>();

        BufferedReader br = null;
        br = new BufferedReader(new FileReader(listePoints));
        try {

            StringBuilder sb = new StringBuilder();

            String line = null;
            line = br.readLine();

            while (line != null) {
                int j = 0;
                try {
                    if (!isOld(new JSONObject(line))) {
                        result.add(new JSONObject(line));
                    }
                } catch (JSONException ex) {

                } finally {
                    try {
                        line = br.readLine();
                    } catch (IOException ex) {

                    }

                }

            }
        } finally {
            br.close();
        }

        // Sortiert die Arraylist nach Uhrzeit ohne Datum
        Collections.sort(result,
                new Comparator<JSONObject>() {

                    @Override
                    public int compare(JSONObject a, JSONObject b
                    ) {

                        String wert1;
                        String wert2;

                        //try {
                        wert1 = (String) a.get("time");
                        wert1 = wert1.substring(11);
                        wert2 = (String) b.get("time");
                        wert2 = wert2.substring(11);
                        return wert1.compareTo(wert2);

                        //} catch (JSONException ex) {                }
                        // return 0;
                    }

                }
        );

        return result;
    }

    private java.awt.Point getSollPoint(ArrayList<JSONObject> liste, Date date) {
        ;
//                Date time = new Date();

        String strAktZeit = uhrzeitFormat.format(date);

        try {
            int zielZeit = Messpunkt.dateToTagesZeit(date);

            for (int i = 0; i < liste.size() - 1; i++) {
                Messpunkt mp1 = new Messpunkt(liste.get(i));

                if (mp1.after(date)) {
                    break;
                }
                Messpunkt mp2 = new Messpunkt(liste.get(i + 1));

                if (!mp2.after(date)) {
                    continue;
                }
                // genau dazwischen oder genau drauf.
                System.out.println(mp1);
                System.out.println(mp2);
                java.awt.Point result = calculateDir(date, mp1, mp2);
                return result;

            }
        } catch (Exception ex) {

            System.out.println(ex);
        }
        return null;
    }

    private java.awt.Point calculateDir(Date date, Messpunkt mp1, Messpunkt mp2) {
        //Stahlensatz
        int secGesamt = mp2.getTageszeit() - mp1.getTageszeit(); // zeitdiff
        int secTeil = Messpunkt.dateToTagesZeit(date) - mp1.getTageszeit();

        int dirdiffGesamt = mp2.getDir() - mp1.getDir(); // dir diff
        System.out.println(mp2.getDir() + " - " + mp1.getDir() + "  =  " + dirdiffGesamt);

        int heigthDiffGesamt = mp2.getHeigth() - mp1.getHeigth(); // dir diff
        //
        int delta = ((dirdiffGesamt) * secTeil) / secGesamt;
        int dir = mp1.getDir() + delta;
        //
        delta = ((heigthDiffGesamt) * secTeil) / secGesamt;
        int heigth = mp1.getHeigth() + delta;
        //
        java.awt.Point result = new java.awt.Point(dir, heigth);
        return result;

    }

    public Regler() {

    }

    void setIstPosition(JSONObject position) {
        this.position = position;
        try {
            control();

        } catch (IOException ex) {
            Logger.getLogger(Regler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private java.awt.Point getIstPoint() {
        // {"roll":5,"dir":0,"pitch":-54}
        int dir = position.getInt("dir");
        // in Grad. Drehen um 180 grad. 0 heisst eigenlich 180
        dir = -dir;
        dir += 180;
        if (dir < 0) {
            dir += 360;
        }
        if (dir >= 360) {
            dir -= 360;
        }
        int heigth = position.getInt("pitch");
        heigth = -heigth;
        java.awt.Point result = new java.awt.Point(dir, heigth);
        return result;
    }

    void sendCommand(int cmd) {
        try {
            //TODO Format json
            JSONObject jo = new JSONObject();
            jo.put("cmd", cmd);
            MqttMessage message = new MqttMessage();
            message.setPayload(jo.toString().getBytes());
            try {
                if (client == null) {
                    client = new MqttClient("tcp://duemchen.ddns.net:1883", "joyit");
                }
                if (client.isConnected()) {
                } else {
                    client.connect();
                }
                client.publish("simago/joy", message);
                System.out.println("sendCommand: " + CMD.values()[cmd]);

            } catch (MqttException ex) {

            }

        } catch (JSONException ex) {
        }

    }

    private void control() throws IOException {
        if (System.currentTimeMillis() < nexttime) {
            return;
        }
        nexttime = System.currentTimeMillis() + 5 * 1000;

        java.awt.Point pSoll;
        int hh = Integer.parseInt(hhFormat.format(new Date()));
        if (hh >= 8 && hh <= 17) {
            if (wind > 6.5) {
                pSoll = getSturmPoint();
            } else {
                // nur alle 5 sekunden tun. immer noch entweder dir oder pitch
                ArrayList<JSONObject> liste = loadListe(datei);
                pSoll = getSollPoint(liste, new Date());
//        System.out.println("simple:" + pSoll);
//        java.awt.Point pSoll1 = getSollPointSunCalc(new Date());
//        System.out.println("neu:   " + pSoll1);

//            pSoll = getSollPointSunCalc(new Date());
            }

        } else {
            pSoll = null;
        }
        if (pSoll == null) {
            pSoll = getRuhePoint();
        }

        System.out.println(new Date() + "  SollPos: " + pSoll);
        java.awt.Point pIst = getIstPoint();
        System.out.println(new Date() + "   ISTPos: " + pIst);
        nowDir = !nowDir;
        if (nowDir) {
            //x == dir
            if (Math.abs(pIst.getX() - pSoll.getX()) > 1) {
                if (pIst.getX() > pSoll.getX()) {
                    sendCommand(CMD.LINKS.ordinal());
                } else {
                    sendCommand(CMD.RECHTS.ordinal());
                }
            } else {
                System.out.println("Dir  ok");
            }
        } else {
            // y == pitch
            if (Math.abs(pIst.getY() - pSoll.getY()) > 1) {
                if (pIst.getY() > pSoll.getY()) {
                    sendCommand(CMD.RUNTER.ordinal());
                } else {

                    sendCommand(CMD.HOCH.ordinal());
                }
            } else {
                System.out.println("HÃƒÂ¶he ok");
            }
        }

    }

    private java.awt.Point getRuhePoint() {
        java.awt.Point result = new java.awt.Point(170, 51);
        return result;
    }

    private java.awt.Point getSturmPoint() {
        java.awt.Point result = new java.awt.Point(180, 51);
        return result;
    }

}
