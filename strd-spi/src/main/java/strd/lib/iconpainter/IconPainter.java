package strd.lib.iconpainter;

import java.io.InputStream;

public interface IconPainter extends AutoCloseable {
    IconPainter drawImage(InputStream inputStream);

    IconPainter setColor(int red, int green, int blue);

    IconPainter setFont(String name, int size, FontStyle fontStyle);

    IconPainter drawRect(int x1, int y1, int x2, int y2);

    IconPainter fillRect(int x1, int y1, int x2, int y2);

    IconPainter fillWholeIcon();

    default IconPainter fillWholeIcon(int red, int green, int blue) {
        return setColor(red, green, blue).fillWholeIcon();
    }

    IconPainter drawOval(int x, int y, int width, int height);

    IconPainter fillOval(int x, int y, int width, int height);

    IconPainter drawPolygon(int[] xPoints, int[] yPoints, int nPoints);

    IconPainter fillPolygon(int[] xPoints, int[] yPoints, int nPoints);

    IconPainter writeText(int x, int y, String text);

    IconPainter writeTextCentered(String text, int xMargin, int yMargin);

    default IconPainter writeTextCentered(String text) {
        return writeTextCentered(text, 0, 0);
    }

    default IconPainter setFont(String name, int size) {
        return setFont(name, size, FontStyle.PLAIN);
    }

    default IconPainter setFont(String name) {
        return setFont(name, 16);
    }

    byte[] toDeviceNativeFormat();

    enum FontStyle {
        PLAIN(0),
        BOLD(1),
        ITALIC(2),
        BOLD_ITALIC(3);

        private final int awtStyleConstant;

        FontStyle(int awtStyleConstant) {
            this.awtStyleConstant = awtStyleConstant;
        }

        public int getAwtStyleConstant() {
            return awtStyleConstant;
        }
    }

    @Override
    void close() throws RuntimeException;
}
