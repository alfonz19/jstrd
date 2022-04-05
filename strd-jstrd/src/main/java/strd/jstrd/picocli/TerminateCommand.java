package strd.jstrd.picocli;

import picocli.CommandLine;
import strd.jstrd.Constants;
import strd.jstrd.util.CliUtil;
import strd.jstrd.util.singleinstance.JUniqueSingleInstance;
import strd.jstrd.util.singleinstance.SingleInstance;
import strd.jstrd.util.singleinstance.command.Command;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@CommandLine.Command(description = "Options regarding stopping app",
        usageHelpWidth = 120,
        name = "stop")
public class TerminateCommand implements Runnable{

    private static final Logger log = getLogger(TerminateCommand.class);

    @Override
    public void run() {

        log.debug("Requesting main app to terminate");
        SingleInstance.CommandReply reply = new JUniqueSingleInstance(Constants.LOCK_ID)
                .runCommandOnAlreadyRunningInstance(
                        new Command("terminate"));

        if (reply.isSuccessful()) {
            CliUtil.printSuccess(reply.getMessage());
        } else {
            CliUtil.printError(reply.getMessage());
        }
    }
}
