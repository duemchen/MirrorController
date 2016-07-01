/**
 * Liste der Messpunkte laden dazu die sonnenposition ergänzen dann die beste
 * Lösung finden: Parameter optimieren Parameter für Spiegel und Ziel damit
 * bestimmt Somit kann der Spiegel in Zukunft korrekt positionieren
 *
 *
 */
package de.lichtmagnet.mavenmirrorcontroller.sunpos;

import de.lichtmagnet.mavenmirrorcontroller.Messpunkt;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.Grena3;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
public class StartSunPos {

    private static String datei = "logfile.txt";  // die handgesammelten Sollpositionen

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
                    result.add(new JSONObject(line));
                } catch (JSONException ex) {

                }
                try {
                    line = br.readLine();
                } catch (IOException ex) {

                }

            }
        } finally {
            br.close();
        }

        // Sortiert die Arraylist nach Uhrzeit ohne Datum
        Collections.sort(result, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject a, JSONObject b) {

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

        });

        return result;
    }

    public static void main(String[] args) throws Exception {
        double lat = 53.106350117569;
        double lon = 12.894292481831371;
        double deltaT = 68;
        GregorianCalendar time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        AzimuthZenithAngle sunAngle = Grena3.calculateSolarPosition(time, lat, lon, deltaT, 1000, 20);
        // System.out.println(time.getTime() + " result: " + sunAngle);
        StartSunPos st = new StartSunPos();
        ArrayList<JSONObject> liste = st.loadListe(datei);
        System.out.println(liste);
        // sunpos ergänzen in messpunkten.
        ArrayList<Messpunkt> li = new ArrayList<Messpunkt>();
        for (int i = 0; i < liste.size() - 1; i++) {
            Messpunkt mp = new Messpunkt(liste.get(i));
//            sunAngle = Grena3.calculateSolarPosition(mp.getZeitpunkt(), lat, lon, deltaT, 1000, 20);
//            mp.setSunPos(sunAngle);
            li.add(mp);
        }
        for (Messpunkt mp : li) {
            System.out.println(mp.printA());
        }

        GregorianCalendar t;
        AzimuthZenithAngle sunA;
        Date d;
        //
        //        d = sdf.parse("26.04.2016 16:49:00");
        //        d = new Date();
        //        t = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        //        t.setTime(d);
        //        sunA = Grena3.calculateSolarPosition(t, lat, lon, deltaT, 1000, 20);
        //        System.out.println("A:" + sunA);
        //
        //        t = new GregorianCalendar(TimeZone.getTimeZone("CET"));
        //        t.setTime(d);
        //        sunA = Grena3.calculateSolarPosition(t, lat, lon, deltaT, 1000, 20);
        //        System.out.println("B:" + sunA);
        //
        //
        d = new Date();
        t = new GregorianCalendar(TimeZone.getTimeZone("AST"));
        t.setTime(d);
        t.setTimeZone(TimeZone.getTimeZone("AST"));
        sunA = Grena3.calculateSolarPosition(t, lat, lon, deltaT, 1000, 20);
        System.out.println(t.getTime() + "  " + sunA.getAzimuth() + "   " + (90 - sunA.getZenithAngle()));

    }
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

}
