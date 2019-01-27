/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import de.horatio.common.HoraIni;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.Grena3;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
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

    public static Logger log = Logger.getLogger("ReglerLogger");
    public static Logger logWind = Logger.getLogger("Windlocal");
    private static DecimalFormat df = new DecimalFormat("#.00");
    public static final String INIDATEI = "MirrorControl.ini";
    private static String state = "unbekannt";

    static double getWindMax() {
        return WINDMAX;
    }

    static double getWind() {
        return wind;
    }

    static double getCloud() {
        return cloud;
    }

    static String getState() {
        return state;
    }

    static int getWindLocal() {
        return (int) windLocal;
    }

    private final String AKTIVEFILE = "activ.txt";
    private Date stichtag;
    private int cloudmax;
    private double minLatidue = 0;
    private double azMin = 90;
    private double azMax = 270;
    private int uhrMin;
    private int uhrMax;

    private Regler() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean isStarkWind() {

        if (windLocal > WINDMAX) {
            //WIND
            starkerWind = true;
        } else {
            //Flaute
            if (starkerWind) {
                //nach Starkwind erst später wieder aktivieren. Hysterese 0.5
                if (windLocal < WINDMAX - WINDHYSTERESE) {
                    starkerWind = false;
                }
            }
        }
        return starkerWind;
    }

    private boolean isRuheZeit() {
        int hh = Integer.parseInt(hhFormat.format(new Date()));
        if (hh < uhrMin) {
            return true;
        }
        if (hh > uhrMax) {
            return true;
        }
        return false;
    }

    private boolean isAzimuthRange() {
        double az = getAzimuth();
        if (az < azMin) {
            return false;
        }
        if (az > azMax) {
            return false;
        }
        return true;
    }

    private boolean isCloudly() {
        return cloud > cloudmax;
    }

    // hoch links rechts runter 0,1,2,3
    enum CMD {

        HOCH, LINKS, RECHTS, RUNTER, NEUTRAL
    }
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat uhrzeitFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat hhFormat = new SimpleDateFormat("HH");
    //
    private static double wind = 0;
    private static double windLocal = 0;
    private static double WINDMAX = 5;
    private static double WINDHYSTERESE = 0.4;
    private static double WINDMITTELWERT = 30;
    private static double WINDMITTELWERTPEAK = 1;

    private static boolean starkerWind = false;
    //
    private static double cloud;

    private String path;
    private CMD cmd = CMD.NEUTRAL;

    private String datei;  // die handgesammelten Sollpositionen
    private JSONObject position;
    private MqttClient client;
    private boolean horizontal;
    private long nexttime = 0;

    private boolean isActive() {
        File f = new File(AKTIVEFILE);
        boolean result = f.exists() && !f.isDirectory();
        return result;
    }

    public Regler(String path, JSONObject position, boolean horizontal) {
        try {
            this.WINDMAX = HoraIni.LeseIniInt(INIDATEI, "WIND", "MAX", 5, true);
            this.WINDHYSTERESE = HoraIni.LeseIniInt(INIDATEI, "WIND", "HYSTERESE", 1, true);
            this.WINDMITTELWERT = HoraIni.LeseIniInt(INIDATEI, "WIND", "MITTELWERT", 100, true);
            this.WINDMITTELWERTPEAK = HoraIni.LeseIniInt(INIDATEI, "WIND", "MITTELWERTPEAK", 10, true);
            this.cloudmax = HoraIni.LeseIniInt(INIDATEI, "CLOUD", "MAX", 60, true);
            this.stichtag = sdf.parse(HoraIni.LeseIniString(INIDATEI, "MESSUNGEN", "STICHTAG", "1.11.2016", true));
            this.minLatidue = HoraIni.LeseIniInt(INIDATEI, "SONNE", "LATIDUDE_MIN", 12, true);
            this.azMin = HoraIni.LeseIniInt(INIDATEI, "SONNE", "AZIMUTH_MIN", 110, true);
            this.azMax = HoraIni.LeseIniInt(INIDATEI, "SONNE", "AZIMUTH_MAX", 250, true);
            this.uhrMin = HoraIni.LeseIniInt(INIDATEI, "SONNE", "UHR_MIN", 8, true);
            this.uhrMax = HoraIni.LeseIniInt(INIDATEI, "SONNE", "UHR_MAX", 18, true);

        } catch (ParseException ex) {
            log.error(ex);
        }
        this.horizontal = horizontal;
        this.path = path;
        this.position = position;
        //calibrierung laden
        path = path.replaceFirst("simago/compass", "");
        if (path.equals("")) {
            datei = "logfile.txt";
        } else {
            path = path.replaceFirst("/", "");
            datei = path + ".txt";
        }
        try {
            controlle();
        } catch (IOException ex) {
            log.error("controlle:" + ex);
            ex.printStackTrace();
        }
    }

    private void controlle() throws IOException {
        java.awt.Point pSoll = null;
        // Bei Sturm auf Sturm
        // sonst nach Osten zum Sonnenaufgang zeigen

        if (isStarkWind()) {
            pSoll = getSturmPoint();
        } else {
            if (isCloudly()) {
                pSoll = getCloudPoint();
            } else {
                if (isRuheZeit()) {
                    pSoll = getRuhePoint();
                } else {
                    if (!isAzimuthRange()) {
                        pSoll = getRuhePoint();
                    } else {
                        if (getLatitude() < minLatidue) {
                            pSoll = getRuhePoint();
                        } else {
                            if (!isActive()) {
                                log.info("Regler ist deaktiviert. Datei nicht gefunden: " + AKTIVEFILE);
                                pSoll = getRuhePoint();
                            } else {
                                //REGELN!!
                                state = "Regler";
                                // nur alle 5 sekunden tun. immer noch entweder dir oder pitch
                                ArrayList<JSONObject> liste = loadListe(datei);
                                pSoll = getSollPoint(liste, new Date());
                                if (HoraIni.LeseIniBool(INIDATEI, "REGLER", "CALC", false, true)) {
                                    //umschalten auf den neuen Weg. Sonnenformel verrechnen
                                    pSoll = getSollCalculated(datei, new Date());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (pSoll == null) {
            pSoll = getRuhePoint();
            log.error("FEHLER? LÜCKE");
        }

        java.awt.Point pIst = getIstPoint();
        horizontal = !horizontal;
        if (horizontal) {
            //x == dir
            if (Math.abs(pIst.getX() - pSoll.getX()) > 1) {
                if (pIst.getX() > pSoll.getX()) {
                    setCommand(CMD.LINKS);
                } else {
                    setCommand(CMD.RECHTS);
                }
            } else {
                log.info("Dir  ok");
            }
        } else // y == pitch
        {
            if (Math.abs(pIst.getY() - pSoll.getY()) > 1) {
                if (pIst.getY() > pSoll.getY()) {
                    setCommand(CMD.RUNTER);
                } else {

                    setCommand(CMD.HOCH);
                }
            } else {
                log.info("Höhe ok");
            }
        }

    }

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
        try {
            return (!stichtag.before(akt.getTime()));
        } catch (Exception ex) {

        }
        return tage > 74;

    }

    static void setWindLocal(int count) {
        // alle 10 sec kommt messung
        // wert grösser als mittelwert sofort ersetzen
        // spitzenwert soll dann langsam  anfallen
        double n = 1;
        if (windLocal < count) {
            n = WINDMITTELWERTPEAK;
        } else {
            //mittelwert senkt langsam ab  neuer wert geht nur zum bruchteil ein
            // 5 minuten * 6 werte = 30 werte sollen erst senken
            n = WINDMITTELWERT;

        }
        n = Math.max(n, 1); // nie kleiner 1
        windLocal = ((windLocal * (n - 1)) + count) / n;
        logWind.info("{mw:" + WINDMITTELWERT + ", mwPeak:" + WINDMITTELWERTPEAK + ", hyst:" + WINDHYSTERESE + ", count:" + count + ", mittel: " + df.format(windLocal) + "}"
        );

    }

    public static void setWind(double windx) {
        wind = windx;
    }

    static void setWindMax(int windmax) {
        WINDMAX = windmax;
    }

    static void setCloud(double clod) {
        cloud = clod;
    }

    private void setCommand(CMD cmd) {
        this.cmd = cmd;
    }

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
        String strAktZeit = uhrzeitFormat.format(date);
        try {
            int zielZeit = Messpunkt.dateToTagesZeit(date);
            Messpunkt mpLast = null;

            for (int i = 0; i < liste.size() - 1; i++) {
                Messpunkt mp1 = new Messpunkt(liste.get(i));
                mpLast = mp1;
                if (mp1.after(date)) {
                    break;
                }
                Messpunkt mp2 = new Messpunkt(liste.get(i + 1));
                mpLast = mp2;

                if (!mp2.after(date)) {
                    continue;
                }
                // genau dazwischen oder genau drauf.
                java.awt.Point result = calculateDir(date, mp1, mp2);
                return result;

            }
            if (mpLast != null) {
                java.awt.Point result = calculateDir(date, mpLast, mpLast);
                return result;
            }
        } catch (Exception ex) {

            log.error(ex);
        }
        return null;
    }

    private java.awt.Point calculateDir(Date date, Messpunkt mp1, Messpunkt mp2) {
        //Stahlensatz
        int secGesamt = mp2.getTageszeit() - mp1.getTageszeit(); // zeitdiff
        int secTeil = Messpunkt.dateToTagesZeit(date) - mp1.getTageszeit();

        int dirdiffGesamt = mp2.getDir() - mp1.getDir(); // dir diff

        int heigthDiffGesamt = mp2.getHeigth() - mp1.getHeigth(); // dir diff
        //
        if (secGesamt == 0) {
            java.awt.Point result = new java.awt.Point(mp1.getDir(), mp1.getHeigth());
            return result;
        }

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

    private java.awt.Point getRuhePoint() {
        int x = HoraIni.LeseIniInt(INIDATEI, path, "RUHE-X", 200, true);
        int y = HoraIni.LeseIniInt(INIDATEI, path, "RUHE-Y", 60, true);
        java.awt.Point result = new java.awt.Point(x, y);
        state = "Ruhe";
        return result;
    }

    private java.awt.Point getCloudPoint() {
        int x = HoraIni.LeseIniInt(INIDATEI, path, "CLOUD-X", 190, true);
        int y = HoraIni.LeseIniInt(INIDATEI, path, "CLOUD-Y", 60, true);
        java.awt.Point result = new java.awt.Point(x, y);
        state = "Bewölkt";
        return result;
    }

    private java.awt.Point getSturmPoint() {
        int x = HoraIni.LeseIniInt(INIDATEI, path, "STURM-X", 210, true);
        int y = HoraIni.LeseIniInt(INIDATEI, path, "STURM-Y", 60, true);
        java.awt.Point result = new java.awt.Point(x, y);
        state = "Sturm";
        return result;
    }

    public String getPath() {
        return path;
    }

    public CMD getCmd() {
        return cmd;
    }

    private Point getSollCalculated(String datei, Date date) {
        PointPos pa, pl;
        // datei ÃƒÆ’Ã‚Â¶ffnen mit erweiterung .calc.txt, json string laden
        // TODO datei einlesen. oder von woher holen
        if (datei.contains("logfile.txt")) {
            pa = new PointPos(datei, "A", 100, 140, 260, 243);
            pl = new PointPos(datei, "L", 10, 13, 60, 28);
        } else {
            pa = new PointPos(datei, "A", 100, 190, 260, 249);
            pl = new PointPos(datei, "L", 10, 2, 50, 22);
        }
        // nach sonnenformel berechnen
        GregorianCalendar time = new GregorianCalendar();
        time.setTime(new Date());
        time.setTimeZone(TimeZone.getTimeZone("GMT"));
        double lat = 53.106350117569;
        double lon = 12.894292481831371;
        double deltaT = 68;
        AzimuthZenithAngle x = Grena3.calculateSolarPosition(time, lat, lon, deltaT, 1000, -10);
        // Umrechnung nach linearer WinkelKorrektur.
        double a = 0.5 + calc(pa, x.getAzimuth());
        double b = 0.5 + calc(pl, 90 - x.getZenithAngle());
        java.awt.Point result = new Point((int) a, (int) b);
        return result;
    }

    private double calc(PointPos pp, double x) {
        double x1 = pp.getX1();
        double y1 = pp.getY1();
        //
        double x2 = pp.getX2();
        double y2 = pp.getY2();
        double result = y1 + (((y2 - y1) / (x2 - x1)) * (x - x1));

        return result;
    }

    private static double getLatitude() {
        // nach sonnenformel berechnen
        GregorianCalendar time = new GregorianCalendar();
        time.setTime(new Date());
        time.setTimeZone(TimeZone.getTimeZone("GMT"));
        double lat = 53.106350117569;
        double lon = 12.894292481831371;
        double deltaT = 68;
        AzimuthZenithAngle x = Grena3.calculateSolarPosition(time, lat, lon, deltaT, 1000, -10);
        double result = 90 - x.getZenithAngle();
        return result;
    }

    private static double getAzimuth() {
        // nach sonnenformel berechnen
        GregorianCalendar time = new GregorianCalendar();
        time.setTime(new Date());
        time.setTimeZone(TimeZone.getTimeZone("GMT"));
        double lat = 53.106350117569;
        double lon = 12.894292481831371;
        double deltaT = 68;
        AzimuthZenithAngle x = Grena3.calculateSolarPosition(time, lat, lon, deltaT, 1000, -10);
        double result = x.getAzimuth();
        return result;
    }

    public static void main(String[] args) throws IOException {
        // Regler r = new Regler();
//        ArrayList<JSONObject> liste = r.loadListe(datei);
//        pSoll = getSollPoint(liste, new Date());
//        //umschalten auf den neuen Weg
//        pSoll = getSollCalculated(datei, new Date());
        getLatitude();
    }

}
