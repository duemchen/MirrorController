package bildholer;

public class SomeService {

    private static boolean stop = false;

    public static void start(String[] args) {
        System.out.println("start");
        TemperatureReader tr = new TemperatureReader();
        TempReadThread instance = new TempReadThread();
        instance.setName("temperaturReader");
        instance.start();
        stop = false;
        while (!stop) {
            try {
                Thread.sleep(1000);
                //stop = true;
                //System.out.print(".");
            } catch (InterruptedException ex) {
                stop = true;
            }
        }
        System.out.print("Beende Bildholer....");
        instance.doStop();
        instance.interrupt();
        System.out.print("....");
    }

    public static void stop(String[] args) {
        System.out.println("stop");
        stop = true;
    }

    public static void main(String[] args) {
        args = new String[1];

        if ("start".equals(args[0])) {
            start(args);
        } else if ("stop".equals(args[0])) {
            stop(args);
        }
        start(args);
    }

}
