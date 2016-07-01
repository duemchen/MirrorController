/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.Grena3;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
public class Messpunkt {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static SimpleDateFormat sdhh = new SimpleDateFormat("HH");
    private static SimpleDateFormat sdmm = new SimpleDateFormat("mm");
    private static SimpleDateFormat sdhhmm = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat sdmmddhhmm = new SimpleDateFormat("MM.dd HH:mm");

    private static SimpleDateFormat sdsec = new SimpleDateFormat("ss");
    private static DecimalFormat dezf = new DecimalFormat("#0.00");

    private final JSONObject zeile;
    private Date zeitpunkt;
    //
    private static double lat = 53.106350117569;
    private static double lon = 12.894292481831371;
    private static double deltaT = 68;

    public static int dateToTagesZeit(Date date) {
        int h = Integer.parseInt(sdhh.format(date));
        int m = Integer.parseInt(sdmm.format(date));
        int sec = Integer.parseInt(sdsec.format(date));
        int result = 60 * 60 * h + 60 * m + sec;
        //   System.out.println(date + "  " + result);
        return result;
    }
    //private AzimuthZenithAngle sunPos;

    public Messpunkt(JSONObject zeile) {
        this.zeile = zeile;
        String s;
        s = this.zeile.getString("time");
        try {
            zeitpunkt = sdf.parse(s);
        } catch (ParseException ex) {
            zeitpunkt = null;
        }
    }

    @Override
    public String toString() {
        return "mp:" + zeile;
    }

    boolean after(Date such) {
        int soll = dateToTagesZeit(such);
        int ist = dateToTagesZeit(zeitpunkt);
        return soll < ist;
    }

    int getTageszeit() {
        return dateToTagesZeit(zeitpunkt);
    }

    int getDir() {
        JSONObject position = (JSONObject) zeile.get("position");
        int result = position.getInt("dir");
        result = -result;
        result += 180;
        if (result < 0) {
            result += 360;
        }

        if (result >= 360) {
            result -= 360;
        }
        return result;
    }

    int getHeigth() {
        JSONObject position = (JSONObject) zeile.get("position");
        int result = position.getInt("pitch");
        result = -result;
        return result;
    }

    public GregorianCalendar getZeitpunkt() {
        GregorianCalendar result = new GregorianCalendar();
        result.setTime(zeitpunkt);
        return result;

    }

//    private void setSunPos(AzimuthZenithAngle sunAngle) {
//        sunPos = sunAngle;
//    }
    public String getUhrzeit() {

        return sdhhmm.format(zeitpunkt);
    }

    private String getDatumUhrzeit() {
        //return sdmmddhhmm.format(zeitpunkt);
        return sdf.format(zeitpunkt);
    }

    /**
     * sollpos des Spiegels nach einer formel berechnet, die sich - aus
     * Koordinaten des Ortes - der dem Ziel - der Messzeit ergeben.
     *
     * Wenn man also einen messpunkt aktueller zeit generiert, kann man damit
     * die aktuelle SollPosition berechnen
     *
     * @return
     */
    private double getSollAzimuth() {
        return getSollAzimuth(getZeitpunkt());
    }

    public static double getSollAzimuth(GregorianCalendar zeitpunkt) {
        AzimuthZenithAngle sunPos = Grena3.calculateSolarPosition(zeitpunkt, lat, lon, deltaT, 1000, 20);
        int targetAzimuth = 150 - 40;  // wo steht das Ziel (Winkelmessung kann eine feste Abweichng haben über den gesamten Messbereich)
        double faktor = 1.3; // völlig unklar
        double result = 0;
        // die Theorie
        result = targetAzimuth + sunPos.getAzimuth(); // versatz zwischen Kompass und richtung
        result = result / 2;
        //
        result = result * faktor;
        return result;
    }

    private double getSollLatitude() {
        return getSollLatitude(getZeitpunkt());
    }

    public static double getSollLatitude(GregorianCalendar zeitpunkt) {
        AzimuthZenithAngle sunPos = Grena3.calculateSolarPosition(zeitpunkt, lat, lon, deltaT, 1000, 20);
        int targetLatitude = 10;  // wo steht das Ziel (Winkelmessung kann eine feste Abweichng haben über den gesamten Messbereich)
        double faktor = 1.0; // völlig unklar
        double result = 0;
        // die Theorie
        result = targetLatitude + 90 - sunPos.getZenithAngle(); // versatz zwischen Kompass und richtung
        result = result / 2;
        //
        result = result * faktor;
        return result;
    }

    public String printA() {
        double delta = (getSollAzimuth() - (double) getDir());
        //String s = getUhrzeit() + ", dir:" + dezf.format(getDir()) + ", sun:" + dezf.format(sunPos.getAzimuth()) + ",  delta:" + dezf.format(d);
        String s = getDatumUhrzeit() + ", dir:" + dezf.format(getDir()) + ", ltsun:" + dezf.format(getSollAzimuth()) + ",  delta:" + dezf.format(delta);
        return s;
    }

    public String printL() {
        double delta = (getSollLatitude() - (double) getHeigth());
        //String s = getUhrzeit() + ", dir:" + dezf.format(getDir()) + ", sun:" + dezf.format(sunPos.getAzimuth()) + ",  delta:" + dezf.format(d);
        String s = getDatumUhrzeit() + ", hei:" + dezf.format(getHeigth()) + ", ltsun:" + dezf.format(getSollLatitude()) + ",  delta:" + dezf.format(delta);
        return s;
    }

}