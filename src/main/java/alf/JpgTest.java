package alf;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JpgTest {
    public static void main(String[] args) throws IOException {
        int width = 100;
        int height = 50;
        BufferedImage off_Image =
                new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = off_Image.createGraphics();
        g2.setColor(Color.red);
        g2.fillRect(0, 0, width, height);
        ImageIO.write(off_Image, "jpg", new File("/tmp/img"));
    }
}
