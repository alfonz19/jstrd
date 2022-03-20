package strd.lib.streamdeck;

import strd.lib.StrdException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO MMUCHA: design of this class is nasty. Revisit.
//we want uniform IconPainter across all implementations and devices, and I don't want to create more than necessary
//buffered images, thus multiple create methods, but I really don't like the call back...
public abstract class AbstractBufferedImageIconPainterFactory implements IconPainterFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractBufferedImageIconPainterFactory.class);

    protected final int iconSize;

    protected AbstractBufferedImageIconPainterFactory(int iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    public IconPainter createEmpty() {
        return new BufferedImageIconPainter(iconSize, this::toDeviceNativeFormatImpl);
    }

    @Override
    public IconPainter create(int red, int green, int blue) {
        return new BufferedImageIconPainter(iconSize, this::toDeviceNativeFormatImpl, red, green, blue);
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

    private static class BufferedImageIconPainter implements IconPainter {

        private final BufferedImage bi;
        private final Graphics2D g2;
        private final int iconSize;
        private final Function<BufferedImage, byte[]> toNativeImageCallback;

        public BufferedImageIconPainter(int iconSize, Function<BufferedImage, byte[]> toNativeImageCallback) {
            this.iconSize = iconSize;
            this.toNativeImageCallback = toNativeImageCallback;
            bi = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB);
            g2 = bi.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //TODO MMUCHA: test!
            g2.setClip(0,0, iconSize, iconSize);
        }

        public BufferedImageIconPainter(int iconSize, Function<BufferedImage, byte[]> toNativeImageCallback, int red, int green, int blue) {
            this(iconSize, toNativeImageCallback);
            setColor(red, green, blue);
            g2.fillRect(0, 0, iconSize, iconSize);
        }

        @Override
        public IconPainter setColor(int red, int green, int blue) {
            g2.setColor(new Color(red, green, blue));
            return this;
        }

        @Override
        public IconPainter setFont(String name, int size) {
            Font font = new Font(/*"Serif"*/null, Font.PLAIN, size);
            g2.setFont(font);
            return this;
        }

        @Override
        public IconPainter fillRect(int x1, int y1, int x2, int y2) {
            g2.fillRect(x1, y1, x2-x1, y2-y1);

            return this;
        }

        @Override
        public IconPainter drawRect(int x1, int y1, int x2, int y2) {
            g2.drawRect(x1, y1, x2-x1, y2-y1);
            return this;
        }

        @Override
        public IconPainter writeText(int x, int y, String text) {

            g2.drawString(text, x,y);
            return this;
        }

        support for multiline!
        @Override
        public IconPainter writeTextCentered(String text) {
            Font initialFont = g2.getFont();

            TextSizeFontAndBounds textSizeAndBounds = findMaxFontSizeForTextToFit(text, initialFont);
            if (textSizeAndBounds == null) {
                return this;
            }

            g2.setFont(textSizeAndBounds.getFont());
            g2.drawString(text,
                    (iconSize - textSizeAndBounds.getWidth()) / 2,
                    textSizeAndBounds.getFm().getAscent() + (iconSize - textSizeAndBounds.getHeight()) / 2);

            return this;
        }

        private TextSizeFontAndBounds findMaxFontSizeForTextToFit(String text, Font initialFont) {
            if (text == null) {
                return null;
            }

            text = text.trim();
            if (text.isEmpty()) {
                return null;
            }

            Font font = initialFont;
            int max = 0;
            int min = 0;
            int iteration = 0;
            int newFontSize;
            int width = 0;
            int height = 0;
            FontMetrics fm = null;

            do {
                if (iteration++ > 15) {
                    log.error("Poor coding, probable infinite loop. You should get your stuff together, man.");
                    break;
                }
                int currentFontSize = font.getSize();
                log.trace("Testing font size: {}",currentFontSize);

                fm = g2.getFontMetrics(font);
                Rectangle2D bounds = fm.getStringBounds(text, g2);
                width = roundUpAndTypecastToInt(bounds.getWidth());
                height = roundUpAndTypecastToInt(bounds.getHeight());
                boolean canFit = width < iconSize && height < iconSize;
                log.trace("Text \"{}\" {} fit into {}} when font size is {}", text, canFit ? "can" : "can't", iconSize, currentFontSize);
                if (!canFit) {
                    max = currentFontSize;
                    newFontSize = min == 0 ? max / 2 : min + (max - min) / 2;

                    log.trace("\t --> trying with smaller font: {}", newFontSize);
                    font = new Font(initialFont.getName(), initialFont.getStyle(), newFontSize);
                } else {
                    min = currentFontSize;
                    newFontSize = max == 0 ? 2*min : min+(max - min) / 2;
                    log.trace("\t --> trying with bigger font: {}", newFontSize);
                    font = new Font(initialFont.getName(), initialFont.getStyle(), newFontSize);
                }
                log.trace("Testing bounds of new size {} against [{}, {}]", newFontSize, min, max);
            } while((max == 0 || newFontSize < max) && (min == 0 || newFontSize > min));

            return new TextSizeFontAndBounds(width, height, font, fm);
        }

        private int roundUpAndTypecastToInt(double value) {
            return Double.valueOf(Math.ceil(value)).intValue();
        }

        @Override
        public final byte[] toDeviceNativeFormat() {
            g2.dispose();
            return toNativeImageCallback.apply(bi);
        }

        private static class TextSizeFontAndBounds {
            private final Font font;
            private final FontMetrics fm;
            private final int width;
            private final int height;

            public TextSizeFontAndBounds(int width, int height, Font font, FontMetrics fm) {
                this.fm = fm;
                if (width == 0 || height == 0) {
                    throw new StrdException("coding error");
                }
                this.width = width;
                this.height = height;
                this.font = font;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            public Font getFont() {
                return font;
            }

            public FontMetrics getFm() {
                return fm;
            }
        }

    }
}
