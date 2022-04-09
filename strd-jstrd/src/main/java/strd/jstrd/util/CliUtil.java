package strd.jstrd.util;

import strd.lib.util.ShutdownHooks;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;

import static org.fusesource.jansi.Ansi.ansi;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * helper class for printing to terminal
 */
public class CliUtil {

    private static final Logger log = getLogger(CliUtil.class);

    private static CliOutput jansiOutput = null;
    private static final CliOutput plainTextCliOutput = new PlainTextCliOutput();
    private static CliOutput cliOutput = plainTextCliOutput;



    //hide me!
    private CliUtil() {}

    public static synchronized void setUseJansiOutput(boolean enableJansi) {
        if (enableJansi) {
            if (jansiOutput == null) {
                //lazy initialization; we do so, if jansi pose a problem for system, we can avoid initializing it.
                jansiOutput = new JansiCliOutput();
            }
            cliOutput = jansiOutput;
        } else {
            cliOutput = plainTextCliOutput;
        }
    }

    public static void printError(String message) {
        cliOutput.printError(message);
    }

    public static void printSuccess(String message) {
        cliOutput.printSuccess(message);
    };

    private static class PlainTextCliOutput implements CliOutput {
        @Override
        public void printError(String message) {
            System.err.println("[ERROR] "+message);
        }

        @Override
        public void printSuccess(String message) {
            System.out.println("[SUCCESS] "+message);
        }
    }
    private static class JansiCliOutput implements CliOutput {
        private static boolean jansiInstalled;

        public JansiCliOutput() {
            enableJansi();
        }

        @Override
        public void printError(String message) {
            System.err.println(ansi().fgRed().a("[ERROR] ").reset().a(message));
        }

        @Override
        public void printSuccess(String message) {
            System.err.println(ansi().fgGreen().a("[SUCCESS] ").reset().a(message));
        }

        private void enableJansi() {
            if (jansiInstalled) {
                return;
            }

            log.debug("installing jansi");
            AnsiConsole.systemInstall();
            ShutdownHooks.register(AnsiConsole::systemUninstall);
            jansiInstalled = AnsiConsole.isInstalled();
        }
    }
}