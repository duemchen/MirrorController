/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lichtmagnet.mavenmirrorcontroller;

/**
 *
 * @author duemchen
 */
class CompassConnectorThread extends Thread implements CompassCallback{

    CompassCallback callback;
    private CompassReader tr;

    
    public CompassConnectorThread() {
    }

    
    void register(CompassCallback x) {
        callback = x;
        x.setPosition("waiting for Compass...");

    }

    
    @Override
	public void run() {

		while (true) {
			tr = new CompassReader();

			tr.register(this);
			try {
				tr.connectToMQTT();
                                
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

    @Override
    public void setPosition(String s) {
        callback.setPosition(s);
    }

   
  
    
    

}
