/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx1;

import javafx.scene.control.Label;

/**
 *
 * @author duemchen
 */
class RunLater implements Runnable {

    private final Label position;
    private final String s;

    RunLater(Label position, String s) {
        this.position = position;
        this.s = s;
        // System.out.println("pos: "+this.position.getText()+"  "+new Date());
    }

    @Override
    public void run() {
        position.setText(s);
    }

}
