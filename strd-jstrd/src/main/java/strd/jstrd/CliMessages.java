package strd.jstrd;

import strd.jstrd.util.CliUtil;

public class CliMessages {

    private CliMessages() {
    }

    public static void error_unableToFindAnyHidLibrary() {
        CliUtil.printError("Unable to find any HID library on classpath.");
    }
}
