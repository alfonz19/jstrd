package strd.jstrd.picocli;

import picocli.CommandLine;

import static picocli.CommandLine.Spec.Target.MIXEE;

public class GlobalMixin {
    private @CommandLine.Spec(MIXEE)
    CommandLine.Model.CommandSpec mixee; // spec of the command where the @Mixin is used

    /**
     * Sets the specified jansi on the LoggingMixin of the top-level command.
     * @param jansi the new jansi value
     */
    @CommandLine.Option(names = {"-j", "--jansi"}, description = "Will enable/disable coloring of output (default: ${DEFAULT-VALUE})", defaultValue = "true")
    public void setJansi(boolean jansi) {
        getParent().setJansi(jansi);
    }

//    public boolean isJansi() {
//        return getParent().isJansi();
//    }

    @CommandLine.Option(names = "--help", usageHelp = true, description = "display this help and exit")
    boolean help;

    private GlobalCommandParent getParent() {
        return (GlobalCommandParent) mixee./*root().*/userObject();
    }
}
