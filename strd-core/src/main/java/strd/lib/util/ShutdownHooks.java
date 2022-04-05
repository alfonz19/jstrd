package strd.lib.util;

import java.util.LinkedList;
import java.util.List;

public class ShutdownHooks {

    //hide me!
    private ShutdownHooks() {
    }

    private static List<Runnable> actions;
    public static void register(Runnable runnable) {
        if (actions == null) {
            actions = new LinkedList<>();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> actions.forEach(Runnable::run)));
        }

        actions.add(runnable);
    }
}
