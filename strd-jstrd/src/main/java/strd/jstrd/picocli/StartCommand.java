package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.CliMessages;
import strd.jstrd.Constants;
import strd.jstrd.streamdeck.Daemon;
import strd.jstrd.configuration.ConfigurationParser;
import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.exception.JstrdException;
import strd.jstrd.util.CliUtil;
import strd.jstrd.util.ServiceLoaderUtil;
import strd.jstrd.util.singleinstance.JUniqueSingleInstance;
import strd.jstrd.util.singleinstance.SingleInstance;
import strd.jstrd.util.singleinstance.command.Command;
import strd.jstrd.util.singleinstance.command.StartInstanceCommand;
import strd.lib.spi.hid.HidLibrary;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("DefaultAnnotationParam")
@CommandLine.Command(description = "Options regarding starting app",
        usageHelpWidth = 120,
        name = "start")
public class StartCommand extends GlobalCommandParent implements Runnable {

    private static final Logger log = getLogger(StartCommand.class);


    @CommandLine.Option(names = {"-t", "--no-tray"}, description = "Will disable systray icon", defaultValue = "false")
    private boolean withoutSystray;

    @CommandLine.Option(names = {"-k",
            "--no-global-keyboard-hook"}, description = "If global keyboard hook library is available, this flag will cause it's not used", defaultValue = "false")
    private boolean withoutKeyHook;

    @CommandLine.Option(names = {"-c", "--load-configuration"}, description = "Configuration to load")
    private File configurationFile;

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
        Optional<HidLibrary> library = ServiceLoaderUtil.getLibrary(HidLibrary.class);
        if (library.isEmpty()) {
            CliMessages.error_unableToFindAnyHidLibrary();
            throw new JstrdException("no hid library found");
        }

        StartDaemonCommand startCommand = new StartDaemonCommand(library.get(),
                configurationFile,
                withoutSystray,
                withOrWithoutUIExclusiveGroup.optionsForAppRunWithUiDisabled.noUi,
                withOrWithoutUIExclusiveGroup.optionsForAppRunWithUiEnabled.openUiOnStart,
                withoutKeyHook);

        boolean started = new JUniqueSingleInstance(Constants.LOCK_ID).startSingleInstanceUsingCommand(startCommand);

        if (!started) {
            CliUtil.printError("Unable to start, app is probably already running");
        }
    }

    private static class StartDaemonCommand extends StartInstanceCommand {

        private final Daemon daemon;

        private final boolean withoutSystray;
        private final boolean withoutUi;
        private final boolean openUiOnStartup;
        private final boolean withoutKeyHook;
        private final File configurationFile;
        private final HidLibrary library;

        public StartDaemonCommand(HidLibrary library,
                                  File configurationFile,
                                  boolean withoutSystray,
                                  boolean withoutUi,
                                  boolean openUiOnStartup,
                                  boolean withoutKeyHook) {
            super("startDaemon");
            this.withoutSystray = withoutSystray;
            this.withoutUi = withoutUi;
            this.openUiOnStartup = openUiOnStartup;
            this.withoutKeyHook = withoutKeyHook;
            this.configurationFile = configurationFile;
            this.library = library;
            daemon = new Daemon(library, withoutKeyHook);
            daemon.setConfiguration(getStreamDeckConfiguration());
        }

        @Override
        public void invoke() {
            daemon.start();
            //TODO MMUCHA: implement.
//            initUI(withoutSystray, withoutUi, openUiOnStartup);
        }

        private StreamDeckConfiguration getStreamDeckConfiguration() {
            if (this.configurationFile == null) {
                CliUtil.warning("Starting with empty configuration file");
                return new StreamDeckConfiguration();
            } else {
                return new ConfigurationParser().parse(this.configurationFile);
            }
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
    }
}
