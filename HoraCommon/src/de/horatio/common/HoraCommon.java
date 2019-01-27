/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.horatio.common;

/**
 *
 * @author duemchen
 */
public class HoraCommon {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HoraIni.LeseIniString("d:/my.ini", "Datenbank", "user", "default", true);
        String s = HoraIni.LeseIniString("d:/my.ini", "Datenbank", "passwort", "default", true);
        System.out.println(s);

    }

}
