package strd.lib;

public class WaitUntilNotTerminated {

    private boolean terminated = false;
    private final int millis;
    private boolean started = false;

    public WaitUntilNotTerminated() {
        this(500);
    }

    public WaitUntilNotTerminated(int millis) {
        this.millis = millis;
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> terminated = true));

        while (!terminated) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void terminate() {
        terminated = true;
    }
}
