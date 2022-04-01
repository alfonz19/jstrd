package strd.jstrd;

import strd.lib.util.WaitUntilNotTerminated;

import java.io.File;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StreamDeckDaemon {
    private static final Logger log = getLogger(StreamDeckDaemon.class);

    private final WaitUntilNotTerminated wunt = new WaitUntilNotTerminated();

    public StreamDeckDaemon() {

    }

    public void start(File configuration, boolean withoutKeyHook) {
        log.debug("Starting with configuration {} and keybordHook={}", configuration, withoutKeyHook);
        wunt.start();
    }

    public void stop() {
        wunt.terminate();
    }
}
