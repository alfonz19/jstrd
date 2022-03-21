package strd.lib.streamdeck;

public interface IconPainterFactory {

    boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant);

    IconPainter createEmpty();
    IconPainter create(int red, int green, int blue);
//    IconPainter create(int pixelCountPerIconSide, File file);
//    IconPainter create(int pixelCountPerIconSide, URI uri);

    interface IconPainter {
        IconPainter setColor(int red, int green, int blue);
        IconPainter setFont(String name, int size);
        IconPainter drawRect(int x1, int y1, int x2, int y2);
        IconPainter fillRect(int x1, int y1, int x2, int y2);
        IconPainter drawOval(int x, int y, int width, int height);
        IconPainter fillOval(int x, int y, int width, int height);
        IconPainter drawPolygon(int[] xPoints, int[] yPoints, int nPoints);
        IconPainter fillPolygon(int[] xPoints, int[] yPoints, int nPoints);
        IconPainter writeText(int x, int y, String text);
        IconPainter writeTextCentered(String text, int xMargin, int yMargin);

        default IconPainter writeTextCentered(String text) {
            return writeTextCentered(text, 0, 0);
        }

        byte[] toDeviceNativeFormat();
    }
}
