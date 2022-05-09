package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.util.CliUtil;

public class GlobalCommandParent {
    @CommandLine.Mixin
    protected GlobalMixin globalMixin;

    public void setJansi(boolean jansi) {
        CliUtil.setUseJansiOutput(jansi);
    }

    public void setVerbose(boolean verbose) {
        CliUtil.setVerbose(verbose);
    }
}
