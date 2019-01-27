/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import de.horatio.common.HoraIni;

/**
 *
 * @author duemchen
 */
class PointPos {

    private final String inidatei = Regler.INIDATEI;

    public int getX2() {
        return x2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    private final int x1;
    private final int x2;
    private final int y1;
    private final int y2;

    PointPos(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    PointPos(String datei, String al, int x1, int y1, int x2, int y2) {
        this.x1 = HoraIni.LeseIniInt(inidatei, datei, al + "x1", x1, true);
        this.x2 = HoraIni.LeseIniInt(inidatei, datei, al + "x2", x2, true);
        this.y1 = HoraIni.LeseIniInt(inidatei, datei, al + "y1", y1, true);
        this.y2 = HoraIni.LeseIniInt(inidatei, datei, al + "y2", y2, true);
    }

    public String toString() {
        return "XY:(" + x1 + "," + y1 + " (" + x2 + "," + y2 + ")";
    }
}
