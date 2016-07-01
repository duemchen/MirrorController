/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mirrorcontroller;

import java.util.ArrayList;

/**
 *
 * @author duemchen
 */
public class MirrorController {

    private ArrayList<JSONObject> loadListe(String dProgrammejavanetbeansprojectJavaFX2logfi) {

        // Arraylist erstellen
        final ArrayList<JSONObject> result = new ArrayList<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("D:/Programme/java/netbeansproject/JavaFX2/logfile.json"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
        }
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            line = br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (line != null) {
//                    sb.append(line);
            int j = 0;
            try {
                //                        System.out.println(sb);
                result.add(new JSONObject(line));
            } catch (JSONException ex) {
                Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                line = br.readLine();
            } catch (IOException ex) {
                Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        // Sortiert die Arraylist nach Uhrzeit ohne Datum
        Collections.sort(result, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject a, JSONObject b) {

                String wert1 = new String();
                String wert2 = new String();
                String datum = new String();

                try {
                    // das Datum wird wert1 zugewiesen
                    wert1 = (String) a.get("time");
                    // aus dem Datum wird das Ende als substring herausgeschnitten.
                    wert1 = wert1.substring(11);
                    // das Datum wird wert2 zugewiesen
                    wert2 = (String) b.get("time");
                    // aus dem Datum wird das Ende als substring herausgeschnitten.
                    wert2 = wert2.substring(11);
//                                System.out.println("Wert2: " + wert2);

                } catch (JSONException ex) {
                    Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
                }
                // vergleicht wert1 und wert2
                return wert1.compareTo(wert2);
            }

        });

        return result;
    }

    /**
     * @param args the command line arguments Liste der gespeicherten Punkte
     * abfahren. 1. Lineare Schätzung zwischen den Punkten. 2. Später
     * Sonnenformel anwenden.
     */
    public static void main(String[] args) {
        System.out.println("Start Spiegelsteuerung");
        // jede x Sekunden auslösen. (5 Sec)
        // laden Daten
        // pos finden und regeln in Happen: rechts links vor zurück

    }

}
