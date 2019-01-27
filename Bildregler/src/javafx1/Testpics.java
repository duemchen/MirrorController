/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx1;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Encoder;

/**
 *
 * @author duemchen
 */
public class Testpics {

    private void bildSchleife() {
        File f = new File("F:/NetBeansProjekte/pictures");
        File[] fileArray = f.listFiles();
        java.util.Arrays.sort(fileArray);
        for (File file : fileArray) {
            try {
                System.out.println("file: " + file.getCanonicalPath());
                File outputfile = new File(file.getCanonicalPath() + ".jpg");
                BufferedImage bi = ImageIO.read(file);
                System.out.println(" enc" + encodeToString(bi, "jpg"));
                ImageIO.write(bi, "jpg", outputfile);
                bi = ImageIO.read(outputfile);
                System.out.println(" w" + bi.getWidth() + " h" + bi.getHeight());
                byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                // System.out.println(new String(data));
                byte[] x = Base64.encodeBase64URLSafe(data);
                System.out.println("x" + new String(x));
                String b64 = Base64.encodeBase64String(data);
                System.out.println("64" + b64);

//                byte[] backToBytes = Base64.decodeBase64(base64String);
//                InputStream in = new ByteArrayInputStream(backToBytes);
//                BufferedImage bi;
//                bi = ImageIO.read(in);
                //byte[] xx = base64String.getBytes(StandardCharsets.UTF_8);
                // bild, wie es von http kommt
//                RunPic rp = new RunPic(this, data);
//                Platform.runLater(rp);
                try {
                    Thread.sleep(50);
                    break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(JavaFX1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    public Testpics() {
    }

    public static void main(String[] args) throws Exception {
        new Testpics().bildSchleife();
    }

}
