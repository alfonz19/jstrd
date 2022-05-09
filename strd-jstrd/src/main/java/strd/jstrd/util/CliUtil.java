package strd.jstrd.util;

import picocli.CommandLine;
import strd.lib.util.ShutdownHooks;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;

import static org.fusesource.jansi.Ansi.ansi;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * helper class for printing to terminal
 */
@SuppressWarnings({"unused", "squid:S106"})
public class CliUtil {

    private static final Logger log = getLogger(CliUtil.class);

    private static CliOutput jansiOutput = null;
    private static final CliOutput plainTextCliOutput = new PlainTextCliOutput();
    private static CliOutput cliOutput = plainTextCliOutput;

    private static boolean verbose = false;



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

    public static synchronized void setVerbose(boolean verbose) {
        CliUtil.verbose = verbose;
    }

    public static void printError(String message) {
        cliOutput.printError(message);
    }

    public static void printSuccess(String message) {
        cliOutput.printSuccess(message);
    }

    public static void printList(String title, Collection<String> items) {
        cliOutput.printList(title, items);
    }

    public static void warning(String message) {
        cliOutput.printWarning(message);
    }

    public static void printException(Throwable ex) {
        cliOutput.printException(ex);
    }

    private static class PlainTextCliOutput extends AbstractCliOutput implements CliOutput {
        @Override
        public void printError(String message) {
            System.err.println("[ERROR] "+message);
        }

        @Override
        public void printSuccess(String message) {
            System.out.println("[SUCCESS] "+message);
        }

        @Override
        public void printWarning(String message) {
            System.err.println("[WARNING] "+message);
        }

        @Override
        public void printList(String title, Collection<String> items) {
            String newLine = System.lineSeparator();
            String text =
                    items.stream().map(e -> "\t• " + e).collect(Collectors.joining(newLine, title + ":" + newLine, ""));
            System.out.println(text);
        }
    }
    private static class JansiCliOutput extends AbstractCliOutput implements CliOutput {
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

        @Override
        public void printWarning(String message) {
            System.err.println(ansi().fgYellow().a("[WARNING] ").reset().a(message));
        }

        @Override
        public void printList(String title, Collection<String> items) {
            String newLine = System.lineSeparator();
            String text =
                    items.stream().map(e -> "\t• " + e).collect(Collectors.joining(newLine, title + ":" + newLine, ""));
            System.out.println(text);
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

    private static class AbstractCliOutput {
        public void printException(Throwable ex) {
            printError(describeException(null, ex));
        }

        public void printException(String message, Throwable e) {
            printError(describeException(message, e));
        }

        private static Throwable unwrapPicoCliExceptions(Throwable e) {
            Throwable t = e;
            while (t.getCause() != null && t instanceof CommandLine.PicocliException) {
                t = t.getCause();
            }
            return t;
        }

        private String padLines(int i, String text) {
            if (i<0) {
                throw new IllegalArgumentException();
            }

            char[] padding = new char[i];
            Arrays.fill(padding, ' ');
            String paddingString = new String(padding);
            return Arrays.stream(text.split("\n"))
                    .map(e -> paddingString + e)
                    .collect(Collectors.joining(System.lineSeparator()));
        }

        private static String getExceptionStacktrace(Throwable t) {
            if (t == null) {
                return "";
            }
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }

        protected String describeException(String message, Throwable e) {
            boolean descriptiveMessageProvided = message != null && !message.isEmpty();
            if (!verbose) {
                if (descriptiveMessageProvided) {
                    return String.format("Application failed: %s(use --verbose for detailed message).", message);
                } else {
                    return "Application failed(use --verbose for detailed message).";
                }
            }

            Throwable t = AbstractCliOutput.unwrapPicoCliExceptions(e);
            StringBuilder builder = new StringBuilder();
            String exceptionMessage = t.getMessage();
            boolean exceptionHasMessage = exceptionMessage != null;

            builder.append("Application failed");
            if (exceptionHasMessage) {
                builder.append(": ").append(exceptionMessage);
            } else if (descriptiveMessageProvided) {
                builder.append(": ").append(message);
            } else {
                builder.append(":");
            }
            builder.append(System.lineSeparator());
            builder.append(padLines(2, getExceptionStacktrace(t)));

            return builder.toString();
        }


    }
}
