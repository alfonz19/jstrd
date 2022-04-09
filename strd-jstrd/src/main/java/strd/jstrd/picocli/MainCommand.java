package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.util.CliUtil;

@CommandLine.Command(description = "Java implementation of StreamDeck daemon/managing app.",
        name = "JStreamDeck",
        mixinStandardHelpOptions = true,
        usageHelpWidth = 120,
        versionProvider = VersionProvider.class,
        subcommands = {
                StartCommand.class,
                TerminateCommand.class,
                GetAllHidLibrariesCommand.class})
public class MainCommand extends GlobalCommandParent {



}
