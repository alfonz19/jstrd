package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.Constants;
import strd.jstrd.util.CliUtil;
import strd.jstrd.util.singleinstance.JUniqueSingleInstance;
import strd.jstrd.util.singleinstance.SingleInstance;
import strd.jstrd.util.singleinstance.command.Command;
import strd.jstrd.util.singleinstance.command.StartInstanceCommand;
import strd.lib.util.WaitUntilNotTerminated;

import java.io.File;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("DefaultAnnotationParam")
@CommandLine.Command(description = "Options regarding starting app",
        usageHelpWidth = 120,
        name = "start")
public class StartCommand /*implements Callable*/ implements Runnable {

    private static final Logger log = getLogger(StartCommand.class);


    @CommandLine.Option(names = {"-t", "--no-tray"}, description = "Will disable systray icon", defaultValue = "false")
    private boolean withoutSystray;

    @CommandLine.Option(names = {"-k",
            "--no-global-keyboard-hook"}, description = "If global keyboard hook library is available, this flag will cause it's not used", defaultValue = "false")
    private boolean withoutKeyHook;

    @CommandLine.Option(names = {"-c", "--load-configuration"}, description = "Configuration to load")
    private File configuration;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "0..1")
    private final WithOrWithoutUIExclusiveGroup withOrWithoutUIExclusiveGroup = new WithOrWithoutUIExclusiveGroup();

    private static class WithOrWithoutUIExclusiveGroup {
        @CommandLine.ArgGroup(exclusive = false)
        WithoutUIOptions optionsForAppRunWithUiDisabled = new WithoutUIOptions();

        @CommandLine.ArgGroup(exclusive = false)
        WithUIOptions optionsForAppRunWithUiEnabled = new WithUIOptions();
    }

    static class WithoutUIOptions {
        @CommandLine.Option(names = {"-n",
                "--no-ui"}, description = "User interface won't be available, only daemon serving devices", defaultValue = "false")
        private boolean noUi;
    }

    static class WithUIOptions {
        @CommandLine.Option(names = {"-o",
                "--open-ui-on-start"}, description = "If UI is allowed, it will be opened upon start", defaultValue = "false")
        private boolean openUi;
    }

    @Override
    public void run() {
        boolean started = new JUniqueSingleInstance(Constants.LOCK_ID)
                .startSingleInstanceUsingCommand(new StartInstanceCommand("start") {

                    final WaitUntilNotTerminated wunt = new WaitUntilNotTerminated();

                    @Override
                    public void invoke() {
                        wunt.start();
                    }

                    @Override
                    public SingleInstance.CommandReply acceptCommand(Command command) {
                        if (command.getCommandName().equals("terminate")) {
                            log.debug("Terminating app");
                            wunt.terminate();
                            return new SingleInstance.CommandReply("terminated", true, "App was terminated.");
                        } else {
                            return new SingleInstance.CommandReply("Unknown command",
                                    false,
                                    "Unsupported command was issued, ignoring");
                        }
                    }
                });

        if (!started) {
            CliUtil.printError("Unable to start, app is probably already running");
        }
    }
}
