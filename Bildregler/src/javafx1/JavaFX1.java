/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx1;

import de.horatio.common.HoraIni;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import static javafx1.Testpics.encodeToString;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author duemchen
 */
public class JavaFX1 extends Application implements CamCallback, CompassCallback {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JavaFX1.class);
    Stage primaryStage;
    Button btn0, btn1, btn2, btn3;
    Button btn10, btn11, btn12, btn13;
    ImageView imgpic;
    ImageView imgpic1;
    HBox btnbox;
    HBox btnbox1;
    Label position;
    private CamConnectorThread camConnectorThread;
    // private CompassConnectorThread compassConnectorThread;
    private MqttClient client;
    private String MQTTLINK = "duemchen.feste-ip.net:56686";
    private String INIDATEI = "bildregler.ini";

    @Override
    public void setBild(byte[] xb) {

        if (xb == null) {
            primaryStage.setTitle("Empfange null");
        } else {
            primaryStage.setTitle(xb.length + " byte, " + new Date());
            // System.out.println(new String(xb));

            try {

                String base64String = new String(xb);
                // rückwandeln von base64
                byte[] backToBytes = Base64.decodeBase64(base64String);
                InputStream in = new ByteArrayInputStream(backToBytes);
                BufferedImage bi;
                bi = ImageIO.read(in);
                if (bi == null) {
                    System.out.println(" Bild=NULL ");
                } else {
                    //BufferedImage bi2 = analyzePic(bi);
                    Image img = SwingFXUtils.toFXImage(bi, null);
                    imgpic.setImage(img);

                    img = analyzePic(bi);
                    imgpic1.setImage(img);

                }

            } catch (Exception ex) {
                Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        MQTTLINK = HoraIni.LeseIniString(INIDATEI, "MQTT", "LINK_PORT", MQTTLINK, true);
        MQTTLINK = "tcp://" + MQTTLINK;
        //TODO relative ablage und ant

        /*        String libpath = System.getProperty("java.library.path");
         libpath = libpath + ";F:\\NetBeansProjekte\\opencv\\build\\java\\x64";
         System.setProperty("java.library.path",libpath);
         */
        System.out.println("lib: " + Core.NATIVE_LIBRARY_NAME);
        System.out.println("libpath: ");
        String property = System.getProperty("java.library.path");
        StringTokenizer parser = new StringTokenizer(property, ";");
        while (parser.hasMoreTokens()) {
            System.out.println(parser.nextToken());
        }
        System.out.println("libpath End.");
        //
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        //
        btn0 = new Button();
        btn0.setText("hoch");
        btn0.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(0, 0);
            }
        });

        btn1 = new Button();
        btn1.setText("links");
        btn1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(0, 1);
            }
        });
        btn2 = new Button();
        btn2.setText("rechts");
        btn2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(0, 2);
            }
        });
        btn3 = new Button();
        btn3.setText("runter");
        btn3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(0, 3);
            }
        });
        btn10 = new Button();
        btn10.setText("hoch");
        btn10.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(1, 0);
            }
        });

        btn11 = new Button();
        btn11.setText("links");
        btn11.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(1, 1);
            }
        });
        btn12 = new Button();
        btn12.setText("rechts");
        btn12.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(1, 2);
            }
        });
        btn13 = new Button();
        btn13.setText("runter");
        btn13.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendCommand(1, 3);
            }
        });

        position = new Label();
        position.setText("Hallo Compass");
        btnbox = new HBox();
        //btnbox.getChildren().add(position);
        btnbox.getChildren().addAll(btn0, btn1, btn2, btn3);
        btnbox.setSpacing(10);
        btnbox.setPadding(new Insets(10));

        btnbox1 = new HBox();
        btnbox1.getChildren().add(position);
        btnbox1.getChildren().addAll(btn10, btn11, btn12, btn13);
        btnbox1.setSpacing(10);
        btnbox1.setPadding(new Insets(10));

        Image img = new Image("file:bild2.jpg");
        imgpic = new ImageView(img);
        imgpic.setFitHeight(200);
        imgpic.setFitWidth(300);
        imgpic1 = new ImageView();
        imgpic1.setFitHeight(200);
        imgpic1.setFitWidth(300);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        //add components to regions of BorderPane
        HBox picbox = new HBox();
        picbox.setSpacing(5);
        picbox.getChildren().addAll(imgpic, imgpic1);
        //
        root.setTop(picbox);
        root.setCenter(position);
        HBox allBtnbox = new HBox();
        root.setBottom(allBtnbox);
        BorderPane.setAlignment(allBtnbox, Pos.CENTER);
        HBox box = new HBox();
        Label lücke = new Label("   Mirror 2: ");
        allBtnbox.getChildren().addAll(new Label("Spiegel 1: "), btnbox, new Label("   Spiegel 2: "), btnbox1);

        Scene scene = new Scene(root, 640, 400);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
        diaShow();

        //Thread Bilderabholen, der sich selbst immer wieder per mqtt verbindet
        camConnectorThread = new CamConnectorThread();
        camConnectorThread.setMQTT(MQTTLINK);
        camConnectorThread.register((CamCallback) this);
        camConnectorThread.register((CompassCallback) this);
        camConnectorThread.start();

        //Kompass abholen
//        compassConnectorThread = new CompassConnectorThread();
        //      compassConnectorThread.register(this);
        //    compassConnectorThread.start();

        /*
         view = new ImageView(bildLaden());
         //düHBox buttonPanel;        buttonPanel = new HBox(btn, gray);

         BorderPane grundLayout = new BorderPane();
         grundLayout.setCenter(view);
         //       grundLayout.setBottom(buttonPanel);

         Scene scene = new Scene(grundLayout, 500, 500);

         primaryStage.setTitle("Bildbearbeitung");dd
         primaryStage.setScene(scene);
         primaryStage.show();

         */
    }

    public Image bildLaden() {
        Image zwischenBild = null;

        try {
            File input = new File("D:/_piCam/bild.jpg");
            //FileInputStream bi = ImageIO.read(input);
            BufferedImage bi = ImageIO.read(input);

            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, data);

            Mat bild = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
            Imgproc.cvtColor(mat, bild, Imgproc.COLOR_BGR2GRAY);

            byte[] data1 = new byte[bild.rows() * bild.cols() * (int) (bild.elemSize())];
            bild.get(0, 0, data1);
            BufferedImage image1 = new BufferedImage(bild.cols(), bild.rows(), BufferedImage.TYPE_BYTE_GRAY);
            image1.getRaster().setDataElements(0, 0, bild.cols(), bild.rows(), data1);

            File ouptut = new File("D:/xml/grayscale2.jpg");
            //ImageIO.write(image1, "jpg", ouptut);
            BufferedImage gray = image1.getSubimage(0, 0, image1.getTileWidth(), image1.getHeight());
            zwischenBild = SwingFXUtils.toFXImage(gray, null);

        } catch (IOException ex) {
            System.out.println("Fehler beim Bild laden...");
        }
        return zwischenBild;
    }

    /**
     * @param args the command line arguments
     */
    public static void xxmain(String[] args) {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //System.load("F:\\NetBeansProjekte\\opencv\\build\\java\\x64\\opencv_java310.dll");

        //launch(args);
    }

    //private BufferedImage analyzePic(BufferedImage bi) {
    private Image analyzePic(BufferedImage bi) {
        //System.out.println("height:" + bi.getHeight() + ", width:" + bi.getWidth());
        //bi = bi.getSubimage(100, 100, 50, 50);
        // Originalbild nach MAT
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        Mat matOrigin = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        matOrigin.put(0, 0, data);
        //+Core.addWeighted();
        //+Imgproc.threshold(matOrigin, matOrigin, thresh, maxval, type)
        //+Core.bitwise_xor(matOrigin, matOrigin, matOrigin);

        // für graubild
        Mat matManipulate = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
        // in Graubild wandeln von...nach
        // Imgproc.cvtColor(matOrigin, matManipulate, Imgproc.COLOR_BGR2GRAY);
        // Imgproc.blur(matManipulate, matManipulate, new Size(3, 3));
        matManipulate = doCanny(matOrigin);
        //aus Mat in BufferedImagae zurückwandeln
        //  byte[] data1 = new byte[matManipulate.rows() * matManipulate.cols() * (int) (matManipulate.elemSize())];
        //  matManipulate.get(0, 0, data1);
        //  BufferedImage biManipulate = new BufferedImage(matManipulate.cols(), matManipulate.rows(), BufferedImage.TYPE_BYTE_GRAY);
        //  biManipulate.getRaster().setDataElements(0, 0, matManipulate.cols(), matManipulate.rows(), data1);
        // BufferedImage gray = image1.getSubimage(0, 0, image1.getTileWidth(), image1.getHeight());
        //return biManipulate;
        matManipulate = doBackgroundRemoval(matManipulate);
        Rect r = new Rect(230, 10, 55, 40);
        Mat m = new Mat(matOrigin, r);
        //return mat2Image(matOrigin);
        return mat2Image(m);

    }

    private Mat doCanny(Mat frame) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(2, 2));

        // canny detector, with ratio of lower:upper threshold of 3:1
        // Imgproc.Canny(detectedEdges, detectedEdges, 2, 2 * 3);
        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);

        return dest;
    }

    private Mat doBackgroundRemoval(Mat frame) {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;
        //inverse
        thresh_type = Imgproc.THRESH_BINARY;

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    /**
     * Get the average hue value of the image starting from its Hue channel
     * histogram
     *
     * @param hsvImg the current frame in HSV
     * @param hueValues the Hue component of the current frame
     * @return the average Hue value
     */
    private double getHistAverage(Mat hsvImg, Mat hueValues) {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++) {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    void sendCommand(int mirror, int cmd) {
        try {
            //TODO Format json
            JSONObject jo = new JSONObject();
            jo.put("cmd", cmd);
            MqttMessage message = new MqttMessage();
            message.setPayload(jo.toString().getBytes());
            //message.setPayload("JoyIt!".getBytes());
            try {
                if (client == null) {
                    client = new MqttClient(MQTTLINK, "xjoyit");
                }
                if (!client.isConnected()) {
                    client.connect();
                }
                if (mirror == 0) {
                    client.publish("simago/joy/74-DA-38-3E-E8-3C", message);

                } else {
                    client.publish("simago/joy/80-1F-02-ED-FD-A6", message);
                }

            } catch (MqttException ex) {
                log.error(ex);

            }

        } catch (JSONException ex) {
            Logger.getLogger(JavaFX1.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @param s
     */
    @Override
    public void setPosition(String s) {

        RunLater rl = new RunLater(position, s);
        Platform.runLater(rl);
    }

    private void bildSchleife() {
        File f = new File("F:/NetBeansProjekte/pictures");
        File[] fileArray = f.listFiles();
        java.util.Arrays.sort(fileArray);
        for (File file : fileArray) {
            try {
                System.out.println("file: " + file.getCanonicalPath());
                BufferedImage bi = ImageIO.read(file);
                String s = encodeToString(bi, "jpg");

                // System.out.println(" enc" + encodeToString(bi, "jpg").substring(0, 10));
                byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

                //byte[] xx = base64String.getBytes(StandardCharsets.UTF_8);
                // bild, wie es von http kommt
                RunPic rp = new RunPic(this, s.getBytes());
                Platform.runLater(rp);
                try {
                    Thread.sleep(550);
                    //break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void diaShow() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Thread Running");
                bildSchleife();
            }
        };

        thread.start();
    }

}
/*

 simago/compass/74-DA-38-3E-E8-3C
 {"roll":-11,"dir":1,"mirrorid":"2","pitch":-20}
 ergänzen "cmd":"save"
 ergänzen "topic":"simago/compass/74-DA-38-3E-E8-3C"

 Wenn bild gut ist, Speichern einer Position:
 - Lesen der Position
 - ergänzen um cmd und topic
 am Besten eigene Quelle ergänzen "src": "image"


 simago/save
 {"roll":-11,"dir":0,"mirrorid":"2","pitch":-18,"cmd":"save","topic":"simago/compass/74-DA-38-3E-E8-3C"}


 TODO

 - Alles raus opencv in eigene klasse
 - Aufnahme von Bildserie alle 5 Minuten ein Bild zur Kontrolle des Reglers  60/5 = 12/Std also 120 Bilder je Tag  2016-11-18-09_00.jpg  18_25
 - Abspielen der Aufnahmen in der Bildanalyse Zeit oder Tastendruck.
 - Erkennung der Leuchtflecke
 - Anfahren mit einem Spiegel bis zum Optimum
 - Merken der korrekten Position für sonnenlose Tage und Referenz
 - Anfahren mit nächstem Spiegel
 ...
 - kontinuierliche Optimierung in Sonnengangrichtung
 */
