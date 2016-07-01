/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tinker;

import com.tinkerforge.BrickIMUV2;
import com.tinkerforge.IPConnection;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author duemchen
 */
public class Tinker {

    private static final String HOST = "localhost";
    private static final int PORT = 4223;
    private static final String UID = "6k3pHc"; // Change to your UID
    private static Motor moHoehe;
    private static Motor moCompass;
//

    private static double x;
    private static double y;
    private static double sollGrad;
    private static int i = 0;
    private static Constanten.MODE mode = Constanten.MODE.REGLER;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.print("Tinker ist starting.");
        System.out.print(Math.toDegrees(0.25 * Math.PI));
        System.out.print(Math.toDegrees(0.5 * Math.PI));
        System.out.print(Math.toDegrees(1.0 * Math.PI));
        System.out.print(Math.toDegrees(2.0 * Math.PI));
        IPConnection ipcon = new IPConnection(); // Create IP connection
        BrickIMUV2 imu = new BrickIMUV2(UID, ipcon); // Create device object

        ipcon.connect(HOST, PORT); // Connect to brickd
        // Don't use device before ipcon is connected
        // imu.saveCalibration()

        // Add quaternion listener
        imu.addQuaternionListener(new BrickIMUV2.QuaternionListener() {
            public void quaternion(short w, short x, short y, short z) {
                DecimalFormat deciform = new DecimalFormat("000.00");
                double ww = w / 16383.0;
                double xx = x / 16383.0;
                double yy = y / 16383.0;
                double zz = z / 16383.0;
                //String s = "w= " + deciform.format(new Double(ww)) + ",\t x= " + deciform.format(new Double(xx)) + ",\t y= " + deciform.format(new Double(yy)) + ",\t z= " + deciform.format(new Double(zz));
                //   System.out.println(s);
                double xAngle = Math.atan2(2 * yy * ww - 2 * xx * zz, 1 - 2 * yy * yy - 2 * zz * zz);
                double yAngle = Math.atan2(2 * xx * ww - 2 * yy * zz, 1 - 2 * xx * xx - 2 * zz * zz);
                double zAngle = Math.asin(2 * xx * yy + 2 * zz * ww);

                xAngle = Math.toDegrees(xAngle);
                yAngle = Math.toDegrees(yAngle);
                zAngle = Math.toDegrees(zAngle);
                //System.out.println("x: " + deciform.format(xAngle) + ", \ty:" + deciform.format(yAngle) + ", \tz:" + deciform.format(zAngle));

                double yaw = Math.atan2(2 * xx * yy + 2 * ww * zz, ww * ww + xx * xx - yy * yy - zz * zz);
                double pitch = -Math.asin(2 * ww * yy - 2 * xx * zz);
                double roll = -Math.atan2(2 * yy * zz + 2 * ww * xx, -ww * ww + xx * xx + yy * yy - zz * zz);
                yaw = Math.toDegrees(yaw);
                pitch = Math.toDegrees(pitch);
                roll = Math.toDegrees(roll);

                System.out.println("yaw: " + deciform.format(yaw) + ", \tpitch:" + deciform.format(pitch) + ", \troll:" + deciform.format(roll));

            }

        });

        imu.setQuaternionPeriod(0);
        /*
         imu.addOrientationListener(new BrickIMUV2.OrientationListener() {
         public void orientation(short heading, short roll, short pitch) {
         System.out.println("h:" + heading);
         }
         });
         imu.setOrientationPeriod(100);
         */

        imu.addAllDataListener(new BrickIMUV2.AllDataListener() {
            public void allData(short[] acceleration, short[] magneticField,
                    short[] angularVelocity, short[] eulerAngle,
                    short[] quaternion, short[] linearAcceleration,
                    short[] gravityVector, byte temperature,
                    short calibrationStatus) {
                try {
                    DecimalFormat deciform = new DecimalFormat("000.00");
                    double xe = eulerAngle[0] / 16.0;
                    double ye = eulerAngle[1] / 16.0;
                    double ze = eulerAngle[2] / 16.0;
                    //  System.out.println("xe: " + deciform.format(xe) + ", \tpitch:" + deciform.format(ye) + ", \troll:" + deciform.format(ze));
                    switch (mode) {
                        case REGLER:
                            if (isCalibrated(calibrationStatus)) {
                                kreisen(); //regler(xe, ye, ze);
                            } else {
                                kreisen();
                            }

                            break;
                        case HAND:
                           // System.out.println("HANDSteuerung");
                            break;
                        case KREISEN:
                            kreisen();
                            break;
                        default:
                            throw new AssertionError(mode.name());
                    }

                    MqttTinker.getInstance().sendTemps(xe, ye, ze, sollGrad, x, y, calibrationStatus);
                } catch (JSONException ex) {
                    Logger.getLogger(Tinker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private boolean isCalibrated(short calibrationStatus) {
                int calCompass = 3 & calibrationStatus;

                if (calCompass > 1) {
                    return true;
                }
                // kreisen lassen, bis er eingerastet ist
                System.out.println("calStatus: " + Integer.toBinaryString(calibrationStatus) + " CalCompass: " + calCompass);
                return false;

            }

        });

        // Set period for all data callback to 0.1s (100ms)
        // Note: The all data callback is only called every 0.1 seconds
        //       if the all data has changed since the last call!
        imu.setAllDataPeriod(5000);

//        System.out.println("Press key to exit");
//        System.in.read();
//        ipcon.disconnect();
    }

    static void setMotor1(Motor m1) {
        moCompass = m1;
    }

    static void setMotor2(Motor m2) {
        moHoehe = m2;
    }

    static void setSoll(double gradPos, double xx, double yy) {
        x = xx;
        y = yy;
        sollGrad = gradPos;
    }

    private static void reglerAutospiegel(double grad, double pitch, double roll) {
        i++;
        try {

            double dPitch = pitch - y;
            if (i % 2 == 0) {
                dPitch = 0;
            }
            if (Math.abs(dPitch) > 0.5) {
                if (dPitch < 0) {
                    moCompass.lang();
                    moHoehe.lang();

                } else {
                    moCompass.kurz();
                    moHoehe.kurz();
                }
                return;
            }
            double dRoll = roll - x;
            if (Math.abs(dRoll) > 0.5) {
                if (dRoll < 0) {
                    moCompass.kurz();
                    moHoehe.lang();

                } else {
                    moCompass.lang();
                    moHoehe.kurz();

                }
                return;
            }
        } finally {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Tinker.class.getName()).log(Level.SEVERE, null, ex);
            }

            moCompass.stop();
            moHoehe.stop();

        }

    }

    private static void regler(double grad, double pitch, double roll) {
        /*
         grad =0  entspricht 90  (osten)
         grad =grad+90
         if >360
        
         y= -1*x +450
        
         */
        grad = -1 * grad + 450;
        if (grad >= 360) {
            grad -= 360;
        }

        pitch = -pitch; // positiv nach oben von der waagerechten
        System.out.println("Hoehe Soll:" + x + ", ist:" + pitch);

        i++;
        try {
            double dPitch = pitch - x;
            if (i % 2 == 0) {
                dPitch = 0;
            }
            if (Math.abs(dPitch) > 1.0) {
                if (dPitch < 0) {
                    moHoehe.hoch();
                } else {
                    moHoehe.runter();
                }
                return;
            }
            if (true) {
                // return;
            }

            double dGrad = grad - sollGrad;
            System.out.println("Compass Soll:" + sollGrad + ", ist:" + grad);

            if (Math.abs(dGrad) > 1.0) {
                if (dGrad < 0) {
                    moCompass.toWest();

                } else {
                    moCompass.toEast();

                }
                return;
            }

        } finally {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Tinker.class.getName()).log(Level.SEVERE, null, ex);
            }

            moCompass.stop();
            moHoehe.stop();

        }

    }

    private static int phase = -1;
    private static int lastPhase = -1;

    private static void kreisen() {
        /*
         einfach feste Zeiten  10 minuten stunden
         ost
         hoch
         west 
         runter
         usw.
         */
        Calendar cal = new GregorianCalendar();
        int stunde = cal.get(Calendar.HOUR_OF_DAY);
        int STUNDE=7;
        if ((stunde < STUNDE) || (stunde > STUNDE)) {
            moCompass.stop();
            moHoehe.stop();
            moHoehe.hoch();
            return;
        }

        int minute = cal.get(Calendar.MINUTE);

        /*
         for (int j = 0; j < 60; j++) {
         //            minute=j;
         System.out.println(minute + " " + phase);

         }
         */
        phase = minute / 5; // alle 5 minuten wechsel
        phase = phase % 4; // 4 phasen
        phase++;

        if (phase != lastPhase) {
            lastPhase = phase;
            moCompass.stop();
            moHoehe.stop();
            switch (phase) {
                case 1:
                    moCompass.toEast();
                    break;
                case 2:
                    moHoehe.hoch();
                    break;
                case 3:
                    moCompass.toWest();
                    break;
                case 4:
                    moHoehe.runter();
                    break;

                default:; //nix

            }

        }

    }

    static void setModus(Constanten.MODE mymode) {
        mode = mymode;
    }

}

/*
 String s = "Acceleration        x: %.02f y: %.02f z: %.02f m/s²%n"
 + "Magnetic Field      x: %.02f y: %.02f z: %.02f µT%n"
 + "Angular Velocity    x: %.02f y: %.02f z: %.02f °/s%n"
 + "Euler Angle         x: %.02f y: %.02f z: %.02f °%n"
 + "Quaternion          x: %.02f y: %.02f z: %.02f w: %.02f%n"
 + "Linear Acceleration x: %.02f y: %.02f z: %.02f m/s²%n"
 + "Gravity Vector      x: %.02f y: %.02f z: %.02f m/s²%n"
 + "Temperature         %d °C%n"
 + "Calibration Status  %s%n%n";
 System.out.format(s,
 acceleration[0] / 100.0, acceleration[1] / 100.0, acceleration[2] / 100.0,
 magneticField[0] / 16.0, magneticField[1] / 16.0, magneticField[2] / 16.0,
 angularVelocity[0] / 16.0, angularVelocity[1] / 16.0, angularVelocity[2] / 16.0,
 eulerAngle[0] / 16.0, eulerAngle[1] / 16.0, eulerAngle[2] / 16.0,
 quaternion[1] / 16383.0, quaternion[2] / 16383.0, quaternion[3] / 16383.0, quaternion[0] / 16383.0,
 linearAcceleration[0] / 100.0, linearAcceleration[1] / 100.0, linearAcceleration[2] / 100.0,
 gravityVector[0] / 100.0, gravityVector[1] / 100.0, gravityVector[2] / 100.0,
 temperature,
 Integer.toBinaryString(calibrationStatus));
 */
