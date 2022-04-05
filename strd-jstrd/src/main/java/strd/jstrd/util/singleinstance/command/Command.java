package strd.jstrd.util.singleinstance.command;

import java.io.Serializable;

public class Command implements Serializable {
    private final String commandName;

    public Command(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
