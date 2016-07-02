/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

/**
 *
 * @author duemchen
 */
public class Motor {

    private final Constanten.DA l;
    private final Constanten.DA r;
    private final Constanten.DA e;

    Motor(Constanten.DA l, Constanten.DA r, Constanten.DA enable) {
        this.l = l;
        this.r = r;
        this.e = enable;
    }

    void stop() {
        System.out.println("stop");
        l.on();
        r.on();
        e.on();

    }

    void left() {
        System.out.println("left");
        l.off();
        r.on();
        e.off();
    }

    void right() {
        System.out.println("right");
        l.on();
        r.off();
        e.off();
    }

    void kurz() {
        left();
    }

    void lang() {
        right();
    }

    public static void main(String[] args) throws Exception, InterruptedException {
        System.out.println("Hi.");
        Motor mo1 = new Motor(Constanten.DA.R1, Constanten.DA.L1, Constanten.DA.E1);

        while (true) {
            System.out.println("left...");
            mo1.left();
            Thread.sleep(2000);
            System.out.println("stop.");
            mo1.stop();
            Thread.sleep(2000);
            System.out.println(System.currentTimeMillis());
        }
    }

    void hoch() {
        left();
    }

    void runter() {
        right();
    }

    void toWest() {
        right();
    }

    void toEast() {
        left();
        
    }

}

/*
 stop vor umpolung
 pwm per pi4j
 sanftstart und stop, 


 */
