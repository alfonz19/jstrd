package strd.jstrd;

import picocli.CommandLine;
import strd.jstrd.picocli.MainCommand;
import strd.jstrd.util.CliUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main {

    public static void main(String[] args) {
        bridgeJavaUtilLoggingToSlf4j();

        //process command-line arguments.
        processCommandLineArguments(args);
    }

    private static void processCommandLineArguments(String[] args) {
        MainCommand mainCommand = new MainCommand();
        CommandLine commandLine = new CommandLine(mainCommand);
        commandLine.setExecutionExceptionHandler((ex, cmdln, parseResult) -> {
            CliUtil.printException(ex);
            return CommandLine.ExitCode.SOFTWARE;
        });

        int exitCode = commandLine.execute(args);
        if (exitCode != CommandLine.ExitCode.OK) {
            System.exit(exitCode);
        }
    }

    private static void bridgeJavaUtilLoggingToSlf4j() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger("").setLevel(Level.INFO);
    }
}
