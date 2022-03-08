package alf;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CopyJpgTest {
    public static void main(String[] args) throws IOException {
        BufferedImage image = readPhotoFromFile();
        BufferedImage buttonImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buttonImage.createGraphics();
        g2.drawImage(image, 0, 0, (img, infoflags, xx, yy, width, height) -> false);
        g2.dispose();

        ImageIO.write(buttonImage, "jpg", new File("/tmp/out.jpg"));
    }

    private static BufferedImage readPhotoFromFile() throws IOException {
        InputStream resourceAsStream = Main.class.getResourceAsStream("/15test.jpg");
        if (resourceAsStream == null) {
            throw new RuntimeException("unable to read file");
        }
        return ImageIO.read(resourceAsStream);
    }
}
