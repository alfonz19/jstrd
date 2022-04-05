package strd.lib.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class WaitUntilNotTerminated {

    private final int millis;
    private final AtomicBoolean waiting = new AtomicBoolean(false);

    public WaitUntilNotTerminated() {
        this(500);
    }

    public WaitUntilNotTerminated(int millis) {
        this.millis = millis;
    }

    public void start() {
        if (waiting.get()) {
            return;
        }
        waiting.set(true);
        ShutdownHooks.register(this::terminate);

        while (waiting.get()) {
            try {
                //noinspection BusyWait
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void terminate() {
        waiting.set(false);
    }
}
