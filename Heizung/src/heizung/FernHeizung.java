/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heizung;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duemchen
 */
class FernHeizung extends Thread {

    private static SimpleDateFormat sdhh = new SimpleDateFormat("HH");

    private final Heizung.DA da;
    private final Heizung.TEMP tempVorlauf;
    private final Heizung.TEMP tempRuecklauf;
    private final Heizung.TEMP tempSpeicher;
    //
    private double HYSTERESE = 0;//;1;

    private final int PERIODE = 10000;
    private final double VERSTAERKUNG = 1.0;//2;//5;//10; //5  20*delta 5 grad bringen 100% heizung
    //
    private double INTERGALFAKTOR = 0.01;  //
    private double integralAnteil = 25;  //0 solange Abweichung ist, wird hier langsam gegengezogen
    private double lastTemp = 0;
    private double DIFERENTIALFAKTOR = 10;//40;  //

    public FernHeizung(Heizung.TEMP tempVorlauf, Heizung.TEMP tempRuecklauf, Heizung.TEMP tempSpeicher, Heizung.DA da) {
        this.tempVorlauf = tempVorlauf;
        this.tempRuecklauf = tempRuecklauf;
        this.tempSpeicher = tempSpeicher;
        this.da = da;
    }

    @Override
    public void run() {
        while (true) {
            try {
                control();
                //sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("Fernheizung---stop.");
                System.out.flush();

                Logger.getLogger(SolarToSpeicher.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt(); // very important
                break;
            }

        }
    }

    /**
     * Je nach Abweichung vom Sollwert wird das Impulsverhältnis geändert stark
     * heizen Dauerein in Prozent angeben. 10 stufen zyklus 20..60 Sekunden
     *
     * heizungsunterstützung beachten. Wenn dort genug hitze, fernwärme
     * auslassen.
     *
     *
     * @throws InterruptedException
     *
     */
    void control() throws InterruptedException {
        int hh = Integer.parseInt(sdhh.format(new Date()));
        double soll = Constants.getVL();
        // tageszeit
        if ((hh < 5) || (hh > 22)) {
            soll = Constants.getVLNacht();
        }

        // Rücklaufbegrenzung. wenn keine Wärme gebraucht wird, fast aus.
        if (tempRuecklauf.getTempLast().doubleValue() > 35) {
            soll = Constants.getVLSommer();
        }

        da.off();
        if (tempVorlauf.getTempLast().doubleValue() <= 10) {
            Thread.sleep(5000);
            return;
        }

        // erst wenn die Speicher verbraucht sind. kleine hysterese einbauen
        boolean zuHeizen = tempSpeicher.getTempLast().doubleValue() < soll;
        //isSolar=!zuHeizen;
        //zuHeizen = true;
        if (!zuHeizen) {
            //System.out.println(" -> Fernwärme AUS, Speicher ist heiss genug: " + tempSpeicher.getTempLast());
            //anheizen=true;
            Thread.sleep(60000);
            return;
        }

        double ist = tempVorlauf.getTempLast().doubleValue();
        double delta = soll - ist; // wenn positiv heizen
        integralAnteil = integralAnteil + delta * INTERGALFAKTOR;
        if (Math.abs(delta) < HYSTERESE) {
            Thread.sleep(5000);
            return;
        }
//        if (delta > 0) {
//            Thread.sleep(5000);
//            return;
//        }
        // heizen mit einer impulsweite.  je weiter weg, um so mehr
        // 10s  1+9  3+7   8+2
        // delta = Math.abs(delta);
        // bei 5 grd 100%
        double proz = (delta * VERSTAERKUNG);
        double prozAnteil = proz;
        proz += integralAnteil; // um den festen anteil erhöhen (nötige Grundlast auch bei genau der solltemp)
        double diff = ist - lastTemp; // 40 - 50 = -10  starker (pos) anstieg, also weniger heizen
        lastTemp = ist;
        double diffAnteil = diff * DIFERENTIALFAKTOR;
        proz -= diffAnteil;
        proz = Math.min(100, proz);
        proz = Math.max(0, proz);

        int on = (int) ((proz / 100) * PERIODE);
        int off = (int) (((100 - proz) / 100) * PERIODE);
        //  System.out.println("vorlauf:" + tempVorlauf.getTempLast()+" C / "+ tempRuecklauf.getTempLast()+" C, gesamt:" + proz + ", p:" + prozAnteil + ", i:" + integralAnteil + ", d:" + diffAnteil + ", on:" + on + ",  off:" + off);
        if (on > 500) { //Kontaktschonung
            da.on();
        }
        Thread.sleep(on);
        if (off > 500) {
            da.off();
        }
        Thread.sleep(off);
    }

}
