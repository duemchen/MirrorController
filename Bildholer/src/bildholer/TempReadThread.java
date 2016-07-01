/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bildholer;

/**
 *
 * @author duemchen
 */
public class TempReadThread extends Thread implements BildCallback {

	
        
	private static TempReadThread instance;
	private TemperatureReader tr;
	private Double temeratur;
	private String time;

	@Override
	public void run() {

		while (true) {
			tr = new TemperatureReader();

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

	public static TempReadThread getInstance() {
		if (instance == null) {
			instance = new TempReadThread();
			instance.setName("temperaturReader");
			instance.start();
		}

		return instance;
	}

	@Override
	public void setTempValues(String message) {
		System.out.println("callback " + message);
		
        }

	public String getTemperatur() {

		return "" + temeratur;
	}

	public String getTimeStr() {

		return time;
	}
}
