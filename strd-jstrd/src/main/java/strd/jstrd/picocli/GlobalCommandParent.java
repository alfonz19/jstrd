package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.util.CliUtil;

public class GlobalCommandParent {
    @CommandLine.Mixin
    protected GlobalMixin jansiMixin;

//    public boolean isJansi() {
//        return jansiMixin.isJansi();
//    }

    public void setJansi(boolean jansi) {
        CliUtil.setUseJansiOutput(jansi);
    }
}
