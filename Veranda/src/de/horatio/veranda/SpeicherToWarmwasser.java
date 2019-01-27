/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.horatio.veranda;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duemchen
 */
class SpeicherToWarmwasser extends Thread {

    private final Veranda.DA da;
    private final Veranda.TEMP tempSpeicher;
    private final Veranda.TEMP tempWarmwasser;
    //
    private double SP_MIN = 57; // mindesttemp
    private double WW_SOLL = 53; // sollwert ww
    private double HYSTERESE = 1;
    

    public SpeicherToWarmwasser(Veranda.TEMP tempSpeicher, Veranda.TEMP tempWarmwasser, Veranda.DA da) {
        this.tempSpeicher = tempSpeicher;
        this.tempWarmwasser = tempWarmwasser;
        this.da = da;
    }

    @Override
    public void run() {
        while (true) {
            try {
                control();
            } catch (InterruptedException ex) {
                System.out.println("---stop.");
                System.out.flush();

                Logger.getLogger(FussbodenHeizung.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt(); // very important
                break;
            }

        }
    }

    /**
     * wwPumpe einschalten, wenn speichertemp > 50 und ww < 40
     * und
     * sp > ww+5
     *
     */
    void control() throws InterruptedException {
       // System.out.println("s2ww sp:" + tempSpeicher.getTempLast() + ", ww:" + tempWarmwasser.getTempLast() + " ");
        boolean heizen = false;
        if (tempSpeicher.getTempLast().doubleValue() >= SP_MIN) {
            if (tempWarmwasser.getTempLast().doubleValue() < WW_SOLL) {

                heizen = true;
            }
        }
        // Hysterese: Ein erst etwas unter der solltemp, Aus sofort an der solltemp
        if (heizen) {
            if (da.isOff()) {
                double delta = Math.abs(tempWarmwasser.getTempLast().doubleValue() - WW_SOLL);
                if (delta < HYSTERESE) {
                    // noch sehr nah an soll
                    heizen = false;
                   // System.out.println("s2ww delta:" + delta + ", Heizen verzögert ");
                }
            }
        }

        if (heizen) {
            da.on();
        } else {
            da.off();
        }
        Thread.sleep(5000);
    }

}
