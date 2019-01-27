/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.horatio.veranda;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import de.horatio.common.HoraFile;
import de.horatio.common.HoraIni;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import openweather.OpenWeather;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import se.hirt.w1.Sensor;
import se.hirt.w1.Sensors;

/**
 *
 * @author duemchen
 */
public class Veranda {

    private static Logger log = Logger.getLogger(Veranda.class);
    private static final GpioController gpio = GpioFactory.getInstance();
    //
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH");
    final static String datei = "veranda.ini";
    private static MqttVeranda mqttTemperaturen;
    private FussbodenHeizung fusshzg;

    private boolean aussentempToPumpe() {
        double temp = 0;
        try {
            openweather.OpenWeather ow = new OpenWeather();
            double lon = 12.89;
            double lat = 53.09;
            ow.setCoord(lon, lat);
            temp = ow.getTemp();
        } catch (Exception e) {

        }
        //System.out.println("temp:" + temp);
        boolean result = true;
        if (temp > 15) {
            result = false;
            DA.PUMPE.off();

        } else {
            DA.PUMPE.on();
        }
        return result;

    }

    /**
     * iii
     */
    public static enum DA {

        STELLHOT(RaspiPin.GPIO_00), //
        STELLIMP(RaspiPin.GPIO_01), //
        PUMPE(RaspiPin.GPIO_02), //
        A(RaspiPin.GPIO_03),//
        B(RaspiPin.GPIO_04),//
        C(RaspiPin.GPIO_05), //
        D(RaspiPin.GPIO_06), //
        LIVE(RaspiPin.GPIO_10),;

        private GpioPinDigitalOutput pin;

        private DA(Pin pin) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }

            this.pin = gpio.provisionDigitalOutputPin(pin, "", PinState.HIGH);
        }

        public GpioPinDigitalOutput getPin() {
            return pin;
        }

        @Override
        public String toString() {
            String s = super.toString();
            return s.substring(0, 1) + s.substring(1).toLowerCase();
        }

        public void on() {
            pin.low();
        }

        public boolean isOff() {
            return pin.isHigh();

        }

        public boolean isOn() {
            return pin.isLow();
        }

        public void off() {
            pin.high();

        }

    }

    /**
     * jeder fühler wird aktiviert wenn er beim start gefunden wurde - alle IDs
     * sammeln in Hash, sortieren und speichern - laden *
     */
    public enum TEMP {

        ISTWERT,
        KONTROLLWERT;
        private Sensor sensor;
        private Number lastTemp = 0;

        public void setSensor(Sensor sensor) {
            this.sensor = sensor;
        }

        public Number getTemp() {
            if (sensor == null) {
                return 0;
            }
            try {
                lastTemp = sensor.getValue();
                return lastTemp;

            } catch (IOException ex) {
                log.error(ex);
            }
            return null;
        }

        public Number getTempLast() {
            return lastTemp;
        }

        @Override
        public String toString() {
            return "TemperaturSensor " + this.name() + ", sensor:" + sensor;
        }

    }

    private static void initTemperatureSensoren() throws IOException {
        System.out.println("hello " + HoraFile.getCanonicalPath(datei));
        Set<se.hirt.w1.Sensor> sensors = Sensors.getSensors();
        System.out.println(String.format("Found %d sensors!", sensors.size()));
        // jeder Sensor wird mit ID in eine Propertydatei eingetragen.
        // dort erfolgt die Zuordnung zu dem konkreten Sensor
        // FERN ID = 28-000000e46c60
        for (Sensor sensor : sensors) {
            System.out.println(String.format("%s(%s):%3.2f%s",
                    sensor.getPhysicalQuantity(), sensor.getID(),
                    sensor.getValue(), sensor.getUnitString()));
            HoraIni.SchreibeIniString(datei, "SensorIDs", sensor.getID(), String.format("%3.2f", sensor.getValue()));

        }

        for (TEMP temp : TEMP.values()) {

            String id = HoraIni.LeseIniString(datei, "Temperaturen", temp.name(), "", true);
            if ("".equals(id)) {
                continue;
            }
            // diese ID in sensoren suchen und verschalten
            System.out.println(id);
            for (Sensor sensor : sensors) {
                //System.out.println(id + " " + sensor.getID());
                if (!id.equalsIgnoreCase(sensor.getID())) {
                    continue;
                }
                temp.setSensor(sensor);
                break;
            }
        }
        // alle angebunden?

        for (TEMP temp : TEMP.values()) {
            System.out.println(temp);
        }

    }

    public Veranda() throws IOException, InterruptedException {
        PropertyConfigurator.configureAndWatch("log4j.xml", 10000);
        log.info("Start Veranda.");
        MqttVeranda.getInstance().sendHallo();
        initTemperatureSensoren();
        fusshzg = new FussbodenHeizung(TEMP.ISTWERT, TEMP.KONTROLLWERT, DA.STELLHOT, DA.STELLIMP);
        // s2ww = new SpeicherToWarmwasser(TEMP.KONTROLLWERT, TEMP.WARMWASSER, DA.PUMPE);
        fusshzg.setName("FussbodenHeizung");
//        s2ww.setName("speicher2Warmwasser");
        fusshzg.start();
        //      s2ww.start();
        DA.PUMPE.on();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    fusshzg.interrupt();
                    fusshzg.join(1000);

                } catch (InterruptedException ex) {
                    log.error(ex);
                }

                for (DA da : DA.values()) {
                    da.off();
                }
                MqttVeranda.getInstance().sendByebye();
                System.out.println("TschAu.");
                log.info("Tschau.");

            }
        });
        int i = 0;
        int lastHour = -1;
        while (true) {

            Thread.sleep(2000);
            //
            String s = sdf.format(new Date());
            Integer hour = Integer.parseInt(s);
            if (lastHour != hour.intValue()) {
                lastHour = hour.intValue();
                // pumpe abschalten wenn temperatur > 15 grad ist.
                boolean heizen = aussentempToPumpe();
                fusshzg.setRegler(heizen);

            }

//
            if (DA.LIVE.isOn()) {
                DA.LIVE.off();
            } else {
                DA.LIVE.on();
            }

//            System.out.println(i + "  " + HoraTime.dateToStr(new Date()));
            for (TEMP temp : TEMP.values()) {
                temp.getTemp();
            }
//            System.out.println("");
            // da testen
            /*
             if (i % 20 == 0) {
             for (DA da : DA.values()) {
             da.on();
             Thread.sleep(50);
             da.off();
             Thread.sleep(100);
             }
             }*/
            if (i % 2 == 0) {
                try {
                    //TODO minütliche info ans MQTT, nach minutenwechsel
                    MqttVeranda.getInstance().sendTemps();
                } catch (Exception ex) {
                    log.error(ex);
                }

            }
            i++;

        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        Veranda veranda = new Veranda();

    }

}

/*
 TODOS



 */
