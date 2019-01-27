/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx1;

/**
 *
 * @author duemchen
 */
class RunPic implements Runnable {

    private byte[] by;
    private final JavaFX1 fx;

    RunPic(JavaFX1 aThis, byte[] data) {
        by = data;
        fx = aThis;
    }

    @Override
    public void run() {
        fx.setBild(by);
    }

}
