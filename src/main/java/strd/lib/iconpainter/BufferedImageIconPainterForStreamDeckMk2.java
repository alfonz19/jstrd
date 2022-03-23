package strd.lib.iconpainter;

import java.io.InputStream;

public class BufferedImageIconPainterForStreamDeckMk2 extends AbstractBufferedImageIconPainter {

    public BufferedImageIconPainterForStreamDeckMk2(int iconSize) {
        super(iconSize);
    }

    public BufferedImageIconPainterForStreamDeckMk2(int iconSize, InputStream imageByteStream) {
        super(iconSize, imageByteStream);
    }

    @Override
    protected byte[] toDeviceNativeFormatTransformation() {
        return writeJpgWithMaxQuality(flipHorizontallyAndVertically(bi));
    }
}
