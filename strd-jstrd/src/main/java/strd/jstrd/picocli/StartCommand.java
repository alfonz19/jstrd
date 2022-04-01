package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.Constants;
import strd.jstrd.StreamDeckDaemon;
import strd.jstrd.util.CliUtil;
import strd.jstrd.util.singleinstance.JUniqueSingleInstance;
import strd.jstrd.util.singleinstance.SingleInstance;
import strd.jstrd.util.singleinstance.command.Command;
import strd.jstrd.util.singleinstance.command.StartInstanceCommand;

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
        private boolean openUiOnStart;
    }

    @Override
    public void run() {
        StartDaemonCommand startCommand = new StartDaemonCommand()
                .setConfiguration(configuration)
                .setOpenUiOnStartup(withOrWithoutUIExclusiveGroup.optionsForAppRunWithUiEnabled.openUiOnStart)
                .setWithoutKeyHook(withoutKeyHook)
                .setWithoutUi(withOrWithoutUIExclusiveGroup.optionsForAppRunWithUiDisabled.noUi)
                .setWithoutSystray(withoutSystray);

        boolean started = new JUniqueSingleInstance(Constants.LOCK_ID).startSingleInstanceUsingCommand(startCommand);

        if (!started) {
            CliUtil.printError("Unable to start, app is probably already running");
        }
    }

    private static class StartDaemonCommand extends StartInstanceCommand {

        private final StreamDeckDaemon daemon = new StreamDeckDaemon();

        private boolean withoutSystray;
        private boolean withoutUi;
        private boolean openUiOnStartup;
        private boolean withoutKeyHook;
        private File configuration;

        public StartDaemonCommand() {
            super("startDaemon");
        }

        @Override
        public void invoke() {
            daemon.start(configuration, withoutKeyHook);
        }

        @Override
        public SingleInstance.CommandReply acceptCommand(Command command) {
            if (command.getCommandName().equals("terminate")) {
                log.debug("Terminating app");
                daemon.stop();
                return new SingleInstance.CommandReply("terminated", true, "App was terminated.");
            } else {
                return new SingleInstance.CommandReply("Unknown command",
                        false,
                        "Unsupported command was issued, ignoring");
            }
        }

        public StartDaemonCommand setWithoutSystray(boolean withoutSystray) {
            this.withoutSystray = withoutSystray;
            return this;
        }

        public StartDaemonCommand setWithoutUi(boolean withoutUi) {
            this.withoutUi = withoutUi;
            return this;
        }

        public StartDaemonCommand setOpenUiOnStartup(boolean openUiOnStartup) {
            this.openUiOnStartup = openUiOnStartup;
            return this;
        }

        public StartDaemonCommand setWithoutKeyHook(boolean withoutKeyHook) {
            this.withoutKeyHook = withoutKeyHook;
            return this;
        }

        public StartDaemonCommand setConfiguration(File configuration) {
            this.configuration = configuration;
            return this;
        }
    }
}
