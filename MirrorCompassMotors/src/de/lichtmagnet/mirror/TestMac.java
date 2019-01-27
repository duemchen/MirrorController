/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mirror;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

/**
 *
 * @author duemchen
 */
public class TestMac {

    public static void main(String[] args) throws IOException, InterruptedException, Exception {
        System.out.println("Hi.");
        String s = getMacAddress("wlan");

    }

    public static String getMacAddress(String wlan) throws SocketException {
        String result = "";
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            byte[] adr = ni.getHardwareAddress();
            if (adr == null || adr.length != 6) {
                continue;
            }
            String name = ni.getName();
            if (name == null) {
                continue;
            }
            result = String.format("%02X-%02X-%02X-%02X-%02X-%02X",
                    adr[0], adr[1], adr[2], adr[3], adr[4], adr[5]);
            System.out.println(result);
            if (name.toUpperCase().contains("wlan".toUpperCase())) {
                break; // wenn wlan gefunden dann den. Sonst weitersuchen.
            }
        }
        return result;
    }

}
