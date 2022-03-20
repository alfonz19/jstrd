package strd.lib.streamdeck;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO MMUCHA: design of this class is nasty. Revisit.
//we want uniform IconPainter across all implementations and devices, and I don't want to create more than necessary
//buffered images, thus multiple create methods, but I really don't like the call back...
public abstract class AbstractBufferedImageIconPainterFactory implements IconPainterFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractBufferedImageIconPainterFactory.class);
    public static final int MAX_X_MARGIN = 10;
    public static final int MAX_Y_MARGIN = 10;

    //if text is multiline, lets split it, trimming each line, again, no point in printing invisible.
    //we're limiting multiline output to 10 lines. There is not point printing more lines, unless trying
    //to make this code fail.
    public static final int MAX_NUMBER_OF_TEXT_LINES = 10;

    //TODO MMUCHA: externalize constants.
    //anything below this is unreadable.
    private static final int MAX_TEXT_LINE_LENGTH = 10;


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

        @Override
        public IconPainter writeTextCentered(String text, int xMargin, int yMargin) {
            xMargin = Math.min(MAX_X_MARGIN, Math.max(0, xMargin));
            yMargin = Math.min(MAX_Y_MARGIN, Math.max(0, yMargin));

            log.trace("Writing centered text: {}", text);
            //if text is null, nothing to print.
            if (text == null) {
                return this;
            }

            //if text is blank, again, nothing to print.
            text = text.trim();
            if (text.isEmpty()) {
                return this;
            }

            int iconWidth = iconSize - xMargin*2;
            int iconHeight = iconSize - yMargin*2;

            List<String> lines = Arrays.stream(text.split("\n"))
                    .map(String::trim)
                    .map(this::shortenLine)
                    .limit(MAX_NUMBER_OF_TEXT_LINES)
                    .collect(Collectors.toList());
            int lineCount = lines.size();
            log.trace("Text will be split into {} lines.", lineCount);

            //it seems, that all lines has same height regardless of what is written. So to find max height of line
            //we can take any, nonempty one. In case there is single line only, we have guarantee, that it's not empty
            //~it has some length. In case of multiple lines, there might be intermediate empty lines.
            Optional<String> firstNonEmptyLine = lines.stream().filter(e -> e.length() > 0).findFirst();
            if (firstNonEmptyLine.isEmpty()) {
                return this;
            }

            //some line to use to calculate text size. In case of single line, calculation will yield final position.
            String line = firstNonEmptyLine.get();
            //fraction of total icon size, reserved for each of N lines. In case of 1 line, this is trivially whole icon.
            int lineHeight = iconHeight / lineCount;
            log.trace("Each line will be drawn into {} pixels of height", lineHeight);

            //find font producing biggest output which fits into icon.
            DataToPrintMaximizedText dataToPrintMaximizedText = findMaxFontSizeForTextToFit(line, iconWidth, lineHeight);

            //set found maximal font.
            g2.setFont(dataToPrintMaximizedText.getFont());

            if (lineCount == 1) {
                //we already have all what we need, just print the text.
                int x = (iconWidth - dataToPrintMaximizedText.getWidth()) / 2;
                int y = dataToPrintMaximizedText.getFm().getAscent() + (iconHeight - dataToPrintMaximizedText.getHeight()) / 2;
                log.trace("Printing text {} at {}x{}", line, x, y);
                g2.drawString(line, xMargin + x, yMargin + y);
            } else {
                //here we have FontMetrics with set maximal font, but we need to recalculate size of each line — as we
                //don't know which one we used to calculate size, we just know it was non-empty. In following loop
                //we will calculate bounds of each line and center it.
                FontMetrics fm = dataToPrintMaximizedText.getFm();

                for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                    String ithLine = lines.get(lineIndex);
                    if (ithLine.isEmpty()) {
                        log.trace("{}-th line is empty, skipping", lineIndex);
                        continue;
                    }
                    Rectangle2D bounds = fm.getStringBounds(ithLine, g2);
                    int ithLineWidth = roundUpAndTypecastToInt(bounds.getWidth());
                    int ithLineHeight = roundUpAndTypecastToInt(bounds.getHeight());

                    int x = (iconWidth - ithLineWidth) / 2;

                    int y = lineIndex * lineHeight + (fm.getAscent() + (lineHeight - ithLineHeight) / 2);
                    log.trace("{}-th line contains text {}, which will be printed at {}x{}", lineIndex, ithLine, x, y);
                    g2.drawString(ithLine, xMargin + x, yMargin + y);
                }
            }

            return this;
        }

        private String shortenLine(String text) {
            return text.length() > MAX_TEXT_LINE_LENGTH
                    ? text.substring(0, MAX_TEXT_LINE_LENGTH )
                    : text;
        }

        @Override
        public final byte[] toDeviceNativeFormat() {
            g2.dispose();
            return toNativeImageCallback.apply(bi);
        }

        private DataToPrintMaximizedText findMaxFontSizeForTextToFit(String text, int iconWidth, int lineHeight) {
            Font initialFont = g2.getFont();
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
                boolean canFit = width < iconWidth && height < lineHeight;
                log.trace("Text \"{}\" {} fit into {}} when font size is {}", text, canFit ? "can" : "can't", lineHeight, currentFontSize);
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

            return new DataToPrintMaximizedText(width, height, font, fm);
        }

        private int roundUpAndTypecastToInt(double value) {
            return Double.valueOf(Math.ceil(value)).intValue();
        }

        private static class DataToPrintMaximizedText {
            private final Font font;
            private final FontMetrics fm;
            private final int width;
            private final int height;

            public DataToPrintMaximizedText(int width, int height, Font font, FontMetrics fm) {
                this.fm = fm;
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
