package strd.lib.streamdeck;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public abstract class AbstractBufferedImageIconPainterFactory implements IconPainterFactory {

    protected final int iconSize;

    protected AbstractBufferedImageIconPainterFactory(int iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    public IconPainter createEmpty() {
        return new BufferedImageIconPainter();
    }

    @Override
    public IconPainter create(int red, int green, int blue) {
        return new BufferedImageIconPainter(red, green, blue);
    }

    protected abstract byte[] toDeviceNativeFormatImpl(BufferedImage bi);

    protected BufferedImage flipHorizontallyAndVertically(BufferedImage image) {
        BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2 = transformed.createGraphics();
        g2.drawImage(image, iconSize, iconSize, -1 * iconSize, -1 * iconSize, null);
        g2.dispose();
        return transformed;
    }

    protected byte[] writeJpgWithMaxQuality(BufferedImage image) {
        try {
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(1.0f);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageOutputStream outputStream = new MemoryCacheImageOutputStream(bos);
            jpgWriter.setOutput(outputStream);
            IIOImage outputImage = new IIOImage(image, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class BufferedImageIconPainter implements IconPainter {

        private final BufferedImage bi;
        private final Graphics2D g2;

        public BufferedImageIconPainter() {
            bi = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB);
            g2 = bi.createGraphics();
            //TODO MMUCHA: test!
            g2.setClip(0,0, iconSize, iconSize);
        }

        public BufferedImageIconPainter(int red, int green, int blue) {
            this();
            g2.setColor(new Color(red, green, blue));
            g2.fillRect(0, 0, iconSize, iconSize);
        }

        @Override
        public IconPainter fillRect(int x1, int y1, int x2, int y2, int red, int green, int blue) {
            g2.setColor(new Color(red, green, blue));
            g2.fillRect(x1, y1, x2-x1, y2-y1);
            return this;
        }

        @Override
        public IconPainter drawRect(int x1, int y1, int x2, int y2, int red, int green, int blue) {
            g2.setColor(new Color(red, green, blue));
            g2.drawRect(x1, y1, x2-x1, y2-y1);
            return this;
        }

        @Override
        public final byte[] toDeviceNativeFormat() {
            g2.dispose();
            return toDeviceNativeFormatImpl(bi);
        }


    }
}