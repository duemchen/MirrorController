package de.lichtmagnet.mavenmirrorcontroller;

public class ReglerService {

    private static ReglerStart start;

    public static void start(String[] args) {
        System.out.println("Start Spiegelsteuerung");
        ReglerStart start = new ReglerStart();
        System.out.println("end");
        System.exit(0);

    }

    public static void stop(String[] args) {
        System.out.println("stop");
        ReglerStart.stopRegler();
    }

    public static void main(String[] args) {
        if ("start".equals(args[0])) {
            System.out.println("start per parameter");
            start(args);
        } else if ("stop".equals(args[0])) {
            System.out.println("stop per parameter");
            stop(args);
        } else {
            System.out.println("start ohne parameter");
            start(args);
        }
    }

}
