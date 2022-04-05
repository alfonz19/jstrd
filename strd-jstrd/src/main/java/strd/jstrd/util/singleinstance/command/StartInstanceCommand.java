package strd.jstrd.util.singleinstance.command;

import strd.jstrd.util.singleinstance.SingleInstance;

public abstract class StartInstanceCommand extends Command {

    public StartInstanceCommand(String commandName) {
        super(commandName);
    }

    public abstract void invoke();

    public abstract SingleInstance.CommandReply acceptCommand(Command command);
}
