package strd.jstrd;

import strd.jstrd.util.CliUtil;

public class CliMessages {

    private CliMessages() {
    }

    public static void printErrorUnableToFindAnyHidLibrary() {
        CliUtil.printError("Unable to find any HID library on classpath.");
    }

    public static void printErrorUnableToFindAnyIconPainterLibrary() {
        CliUtil.printError("Unable to find any IconPainter library on classpath.");
    }
}
