package strd.jstrd.util.singleinstance;

import strd.jstrd.util.singleinstance.command.Command;
import strd.jstrd.util.singleinstance.command.StartInstanceCommand;

import java.io.Serializable;
import java.util.Objects;

public interface SingleInstance {

    CommandReply runCommandOnAlreadyRunningInstance(Command command);
    boolean startSingleInstanceUsingCommand(StartInstanceCommand startCommand);

    final class CommandReply implements Serializable {
        private final String id;
        private final boolean successful;
        private final String message;

        public CommandReply(String id, boolean successful, String message) {
            this.id = Objects.requireNonNull(id);
            this.successful = successful;
            this.message = Objects.requireNonNull(message);
        }

        public String getId() {
            return id;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getMessage() {
            return message;
        }
    }
}
