package strd.lib.iconpainter;

import strd.lib.Constants;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractBufferedImageIconPainter implements IconPainter {
    
    private static final Logger log = getLogger(AbstractBufferedImageIconPainter.class);

    protected final BufferedImage bi;
    private final Graphics2D g2;
    private final int iconSize;

    private AbstractBufferedImageIconPainter(int iconSize, BufferedImage bi) {
        this.iconSize = iconSize;
        this.bi = bi;
        g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(0, 0, iconSize, iconSize);
    }

    protected AbstractBufferedImageIconPainter(int iconSize) {
        this(iconSize, new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB));
    }

    protected AbstractBufferedImageIconPainter(int iconSize, InputStream imageByteStream) {
        this(iconSize, readImageFromStream(iconSize, imageByteStream));
    }

    public AbstractBufferedImageIconPainter(int iconSize, byte[] bytes) {
        this(iconSize, readImageFromStream(iconSize, new ByteArrayInputStream(bytes)));
    }

    private static BufferedImage readImageFromStream(int iconSize, InputStream imageByteStream) {
        try {
            BufferedImage image = ImageIO.read(imageByteStream);
            return image.getWidth() > iconSize || image.getHeight() > iconSize
                    ? image.getSubimage(0, 0, iconSize, iconSize)
                    : image;
        } catch (IOException e) {
            throw new StrdException(e);
        }
    }

    @Override
    public IconPainter setColor(int red, int green, int blue) {
        g2.setColor(new Color(red, green, blue));
        return this;
    }

    @Override
    public IconPainter setFont(String name,
                               int size,
                               FontStyle fontStyle) {
        //noinspection MagicConstant
        Font font = new Font(/*"Serif"*/null, fontStyle.getAwtStyleConstant(), size);
        g2.setFont(font);
        return this;
    }

    @Override
    public IconPainter fillRect(int x1, int y1, int x2, int y2) {
        g2.fillRect(x1, y1, x2 - x1, y2 - y1);

        return this;
    }

    @Override
    public IconPainter fillWholeIcon() {
        g2.fillRect(0, 0, iconSize, iconSize);
        return this;
    }

    @Override
    public IconPainter drawRect(int x1, int y1, int x2, int y2) {
        g2.drawRect(x1, y1, x2 - x1, y2 - y1);
        return this;
    }

    public IconPainter fillOval(int x, int y, int width, int height) {
        g2.fillOval(x, y, width, height);
        return this;
    }

    @Override
    public IconPainter drawOval(int x, int y, int width, int height) {
        g2.drawOval(x, y, width, height);
        return this;
    }

    @Override
    public IconPainter fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g2.fillPolygon(xPoints, yPoints, nPoints);
        return this;
    }

    @Override
    public IconPainter drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g2.drawPolygon(xPoints, yPoints, nPoints);
        return this;
    }

    @Override
    public IconPainter writeText(int x, int y, String text) {

        g2.drawString(text, x, y);
        return this;
    }

    @Override
    public IconPainter writeTextCentered(String text, int xMargin, int yMargin) {
        xMargin = Math.min(Constants.MAX_X_MARGIN, Math.max(0, xMargin));
        yMargin = Math.min(Constants.MAX_Y_MARGIN, Math.max(0, yMargin));

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

        int iconWidth = iconSize - xMargin * 2;
        int iconHeight = iconSize - yMargin * 2;

        List<String> lines = Arrays.stream(text.split("\n"))
                .map(String::trim)
                .map(this::shortenLine)
                .limit(Constants.MAX_NUMBER_OF_TEXT_LINES)
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
        log.trace("Each line will be drawn into {} pixels of height",
                lineHeight);

        //find font producing the biggest output which fits into icon.
        FindMaxFontSize.ResultData maximizedTextSpecs =
                FindMaxFontSize.findMaxFontSizeForTextToFit(this.g2, line, iconWidth, lineHeight);

        //set found maximal font.
        g2.setFont(maximizedTextSpecs.getFont());

        if (lineCount == 1) {
            //we already have all what we need, just print the text.
            int x = (iconWidth - maximizedTextSpecs.getWidth()) / 2;
            int y = maximizedTextSpecs.getFm().getAscent() + (iconHeight - maximizedTextSpecs.getHeight()) / 2;
            log.trace("Printing text {} at {}x{}", line, x, y);
            g2.drawString(line, xMargin + x, yMargin + y);
        } else {
            //here we have FontMetrics with set maximal font, but we need to recalculate size of each line — as we
            //don't know which one we used to calculate size, we just know it was non-empty. In following loop
            //we will calculate bounds of each line and center it.
            FontMetrics fm = maximizedTextSpecs.getFm();

            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String ithLine = lines.get(lineIndex);
                if (ithLine.isEmpty()) {
                    log.trace("{}-th line is empty, skipping", lineIndex);
                    continue;
                }
                Rectangle2D bounds = fm.getStringBounds(ithLine, g2);
                int ithLineWidth = FindMaxFontSize.roundUpAndTypecastToInt(bounds.getWidth());
                int ithLineHeight = FindMaxFontSize.roundUpAndTypecastToInt(bounds.getHeight());

                int x = (iconWidth - ithLineWidth) / 2;

                int y = lineIndex * lineHeight + (fm.getAscent() + (lineHeight - ithLineHeight) / 2);
                log.trace(
                        "{}-th line contains text {}, which will be printed at {}x{}",
                        lineIndex,
                        ithLine,
                        x,
                        y);
                g2.drawString(ithLine, xMargin + x, yMargin + y);
            }
        }

        return this;
    }

    private String shortenLine(String text) {
        return text.length() > Constants.MAX_TEXT_LINE_LENGTH
                ? text.substring(0, Constants.MAX_TEXT_LINE_LENGTH)
                : text;
    }

    @Override
    public IconPainter drawImage(InputStream inputStream) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public final byte[] toDeviceNativeFormat() {
        byte[] result = toDeviceNativeFormatTransformation();
        g2.dispose();
        return result;
    }

    protected abstract byte[] toDeviceNativeFormatTransformation();

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

    //TODO MMUCHA: This class is not nice to look at. It's in this shape to avoid instantiations. Premature optimization, fix.
    private static class FindMaxFontSize {

        private static FindMaxFontSize.ResultData findMaxFontSizeForTextToFit(Graphics2D g2,
                                                                              String text,
                                                                              int iconWidth,
                                                                              int lineHeight) {
            Font initialFont = g2.getFont();
            Font font = initialFont;
            FontMetrics fm;
            int max = 0;
            int min = 0;
            int iteration = 0;
            int newFontSize;
            int width;
            int height;

            FindMaxFontSize.ResultData result = new FindMaxFontSize.ResultData();

            do {
                if (iteration++ > 15) {
                    log.error(
                            "Poor coding, probable infinite loop. You should get your stuff together, man.");
                    break;
                }
                int currentFontSize = font.getSize();
                log.trace("Testing font size: {}", currentFontSize);

                fm = g2.getFontMetrics(font);
                Rectangle2D bounds = fm.getStringBounds(text, g2);
                width = roundUpAndTypecastToInt(bounds.getWidth());
                height = roundUpAndTypecastToInt(bounds.getHeight());
                boolean canFit = width < iconWidth && height < lineHeight;
                log.trace("Text \"{}\" {} fit into {}} when font size is {}",
                        text,
                        canFit ? "can" : "can't",
                        lineHeight,
                        currentFontSize);

                if (!canFit) {
                    max = currentFontSize;
                    newFontSize = min == 0 ? max / 2 : min + (max - min) / 2;

                    log.trace("\t --> trying with smaller font: {}",
                            newFontSize);
                } else {
                    result.set(width, height, font, fm);
                    min = currentFontSize;
                    newFontSize = max == 0 ? 2 * min : min + (max - min) / 2;
                    log.trace("\t --> trying with bigger font: {}",
                            newFontSize);
                }

                font = new Font(initialFont.getName(), initialFont.getStyle(), newFontSize);
                log.trace("Testing bounds of new size {} against [{}, {}]",
                        newFontSize,
                        min,
                        max);
            } while ((max == 0 || newFontSize < max) && (min == 0 || newFontSize > min));

            log.trace("Found maximum with bounding box {}x{}",
                    result.getWidth(),
                    result.getHeight());
            return result;
        }

        private static int roundUpAndTypecastToInt(double value) {
            return Double.valueOf(Math.ceil(value)).intValue();
        }

        private static class ResultData {
            private Font font;
            private FontMetrics fm;
            private int width;
            private int height;

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

            private void set(int width, int height, Font font, FontMetrics fm) {
                this.width = width;
                this.height = height;
                this.font = font;
                this.fm = fm;
            }
        }
    }

}
