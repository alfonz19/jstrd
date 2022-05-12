package strd.jstrd.streamdeck.unfinished.action;

import strd.jstrd.Constants;
import strd.jstrd.util.singleinstance.JUniqueSingleInstance;
import strd.jstrd.util.singleinstance.command.Command;

public class QuitAction implements Action {
    @Override
    public void invoke() {
        ////TODO MMUCHA: refactor to have some util to invoke such commands.
        new JUniqueSingleInstance(Constants.LOCK_ID).runCommandOnAlreadyRunningInstance(new Command("terminate"));
    }
}
