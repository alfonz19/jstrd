package alf;

import java.util.LinkedList;
import java.util.List;


public class ShutdownHooks {

    private ShutdownHooks() {
        //hide me!
    }

    private static List<Runnable> actions;
    public static void register(Runnable runnable) {
        if (actions == null) {
            actions = new LinkedList<>();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                actions.forEach(Runnable::run);
            }));
        }

        actions.add(runnable);
    }
}
