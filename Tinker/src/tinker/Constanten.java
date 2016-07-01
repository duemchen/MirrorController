/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tinker;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 *
 * @author duemchen
 */
public class Constanten {

    public static enum MODE {

        REGLER,
        HAND,
        KREISEN
    }

    private static final GpioController gpio = GpioFactory.getInstance();

    public static enum DA {

        R1(RaspiPin.GPIO_00), //rot
        L1(RaspiPin.GPIO_01), //gelb
        E1(RaspiPin.GPIO_02), //grün
        R2(RaspiPin.GPIO_03),//rot
        L2(RaspiPin.GPIO_04),//gelb
        E2(RaspiPin.GPIO_05), //grün
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

}
