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
class CamConnectorThread extends Thread implements CamCallback, CompassCallback {

    CamCallback callback;
    CompassCallback coCallback;
    private CamReader tr;
    private String MQTTLINK;

    @Override
    public void setBild(byte[] bild) {
        callback.setBild(bild);
    }

    @Override
    public void setPosition(String s) {
        coCallback.setPosition(s);
    }

    public CamConnectorThread() {
    }

    void register(CamCallback x) {
        callback = x;
        x.setBild(null);

    }

    void register(CompassCallback x) {
        coCallback = x;
        x.setPosition("registered");

    }

    @Override
    public void run() {

        while (true) {
            tr = new CamReader();  //empfängt bild und compass
            tr.register((CamCallback) this);
            tr.register((CompassCallback) this);
            try {
                tr.connectToMQTT(MQTTLINK);

            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (tr.isConnected()) {
                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
            System.out.println("restart");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }

    }

    void setMQTT(String MQTTLINK) {
        this.MQTTLINK = MQTTLINK;
    }

}
