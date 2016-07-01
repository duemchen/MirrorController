/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package heizung;

/**
 *
 * @author duemchen
 */
public class Constants {

    static private double sollVL = 45;
    static private double sollVLNacht = 39;
    static private double sollVLSommer = 35;

    public static double getVL() {
        return sollVL;
    }

    public static double getVLNacht() {
        return sollVLNacht;
    }

    static double getVLSommer() {
        return sollVLSommer;
    }

}
