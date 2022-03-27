package strd.lib.iconpainter;

import java.io.InputStream;

public class BufferedImageIconPainterForStreamDeckMk2 extends AbstractBufferedImageIconPainter {

    public BufferedImageIconPainterForStreamDeckMk2(int iconSize) {
        super(iconSize);
    }

    public BufferedImageIconPainterForStreamDeckMk2(int iconSize, InputStream imageByteStream) {
        super(iconSize, imageByteStream);
    }

    public BufferedImageIconPainterForStreamDeckMk2(int iconSize, byte[] bytes) {
        super(iconSize, bytes);
    }

    @Override
    protected byte[] toDeviceNativeFormatTransformation() {
        return writeJpgWithMaxQuality(flipHorizontallyAndVertically(bi));
    }
}
