package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.JsonNode;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Superclass for command handlers.
 */
@Component
public abstract class CommandHandler {

    private Logger log = LogManager.getLogger(this.getClass());

    /**
     * Return the command name that this handler answers to. Must be unique among CommandHandlers; no two CommandHandler classes may return the same string
     * @return
     */
    protected abstract String getHandledCommand();

    /**
     * Returns the command names that this handler answers to. Default implementation is to return getHandledCommand() wrapped in a list.
     * Subclasses will only need to implement this if they really have more than one command name
     * @return
     */
    public List<String> getHandledCommands() {
        return Collections.singletonList(this.getHandledCommand());
    }

    public abstract boolean accept(Command command);

    /**
     * Return a Worker subclass object that will handle the given Command. This worker runs as a thread started by CommandWatcher
     * @param command
     * @return
     * @throws DataFordelerException
     */
    public abstract Worker doHandleCommand(Command command) throws DataFordelerException;

    protected Logger getLog() {
        return this.log;
    }

    /**
     * Given a Command object, write the output for GET and POST requests
     * @param command
     * @return
     */
    public abstract JsonNode getCommandStatus(Command command);


    /**
     * Parses a command body (body of a POST request) into a CommandData subclass object.
     * Implementors must themselves define their CommandData subclass returned by this method
     * @param commandBody
     * @return
     * @throws DataStreamException
     * @throws InvalidClientInputException
     */
    public abstract CommandData getCommandData(String commandBody)
            throws DataStreamException, InvalidClientInputException;
}
