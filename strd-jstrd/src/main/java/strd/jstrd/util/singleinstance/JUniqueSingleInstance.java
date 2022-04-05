package strd.jstrd.util.singleinstance;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import strd.jstrd.util.SerializableUtil;
import strd.jstrd.util.singleinstance.command.Command;
import strd.jstrd.util.singleinstance.command.StartInstanceCommand;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class JUniqueSingleInstance implements SingleInstance {

    private static final Logger log = getLogger(strd.jstrd.util.singleinstance.JUniqueSingleInstance.class);

    private final String lockId;

    public JUniqueSingleInstance(String lockId) {
        this.lockId = lockId;
    }


    @Override
    public CommandReply runCommandOnAlreadyRunningInstance(Command command) {
        try {
            JUnique.acquireLock(lockId);
            log.debug("Single instance not running, not invoking command");
            return new CommandReply("NotInvoked", false, "App is not running, command cannot be invoked.");
        } catch (AlreadyLockedException e) {
            String reply = JUnique.sendMessage(lockId, SerializableUtil.serializableToBase64(command));
            return SerializableUtil.deserializableFromBase64(reply, CommandReply.class);
        }
    }

    @Override
    public boolean startSingleInstanceUsingCommand(StartInstanceCommand startCommand) {
        try {
            JUnique.acquireLock(lockId, message -> {
                Command command = SerializableUtil.deserializableFromBase64(message, Command.class);
                CommandReply commandReply = startCommand.acceptCommand(command);

                return SerializableUtil.serializableToBase64(commandReply);
            });

            log.debug("starting new instance.");
            startCommand.invoke();
            return true;
        } catch (AlreadyLockedException e) {
            log.debug("not starting, app is already running!");
            return false;
        }
    }
}
