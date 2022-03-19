package strd.lib.streamdeck;

import strd.lib.StdrException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public interface IconPainterFactory {

    void setStreamDeckVariant(StreamDeckVariant streamDeckVariant);

//    IconPainter createEmpty();
    IconPainter create(int red, int green, int blue);
//    IconPainter create(int pixelCountPerIconSide, File file);
//    IconPainter create(int pixelCountPerIconSide, URI uri);

    interface IconPainter {
        byte[] toDeviceNativeFormat();
    }

    public static class BufferedImageIconPainterFactory implements IconPainterFactory {

        private StreamDeckVariant streamDeckVariant;
        private int iconSize;

        @Override
        public void setStreamDeckVariant(StreamDeckVariant streamDeckVariant) {
            this.streamDeckVariant = streamDeckVariant;
            iconSize = streamDeckVariant.getPixelCountPerIconSide();
        }

//        @Override
//        public byte[] createColoredIcon(int red, int green, int blue) {
//            BufferedImage bi = createdColoredBufferedImage(red, green, blue);
//
//            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//                ImageIO.write(bi, "jpg", bos);
//                g2.dispose();
//                return bos.toByteArray();
//            } catch (IOException e) {
//                throw new RuntimeException("write failed");
//            }
//
//        }

        @Override
        public IconPainter create(int red, int green, int blue) {
            assertSetStreamDeckVariant();
            return new BufferedImageIconPainter(red, green, blue);
        }



        private void assertSetStreamDeckVariant() {
            if (streamDeckVariant == null) {
                throw new StdrException("Stream deck variant not set");
            }
        }

        private class BufferedImageIconPainter implements IconPainter {

            private final BufferedImage bi;
            private final Graphics2D g2;


            public BufferedImageIconPainter(int red, int green, int blue) {
                bi = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB);
                g2 = bi.createGraphics();
                g2.setColor(new Color(red, green, blue));
                g2.fillRect(0, 0, iconSize, iconSize);
            }

            @Override
            public byte[] toDeviceNativeFormat() {
                g2.dispose();
                return writeJpgWithMaxQuality(flipHorizontallyAndVertically(bi));
            }

            private BufferedImage flipHorizontallyAndVertically(BufferedImage image) {
                BufferedImage transformed = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                Graphics2D g2 = transformed.createGraphics();
                g2.drawImage(image, iconSize, iconSize, -1*iconSize, -1*iconSize, null);
                g2.dispose();
                return transformed;
            }

            private byte[] writeJpgWithMaxQuality(BufferedImage image) {
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
        }
    }
}
