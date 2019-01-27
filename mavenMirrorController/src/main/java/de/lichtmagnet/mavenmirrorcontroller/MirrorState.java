/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

import de.horatio.common.HoraTime;
import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author duemchen
 */
class MirrorState {

    private Date last = new Date();
    private final String mac;
    private String name = null;

    MirrorState(String mac) {
        this.mac = mac;
    }

    void setTime(Date date) {
        last = date;

    }

    public String toString() {
        return "MState " + name + ": " + mac + ", " + last;
    }

    JSONObject getJson() {
        JSONObject result = new JSONObject();
        if (name == null) {
            name = mac;
        }
        result.put("name", name);
        result.put("mac", mac);
        result.put("status", getState());
        return result;
    }

    /**
     *
     * @return kommen noch Compass-Werte? true es kommen welche
     */
    private boolean getState() {
        return (new Date().getTime() - last.getTime()) < 30 * HoraTime.C1SEKUNDE;

    }
}
