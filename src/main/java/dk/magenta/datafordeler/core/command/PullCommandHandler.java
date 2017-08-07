package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.PluginNotFoundException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by lars on 29-05-17.
 */
@Component
public class PullCommandHandler extends CommandHandler {

    public static class PullCommandData {

        public PullCommandData() {
        }

        @JsonProperty
        public String plugin;
    }

    @Autowired
    private Engine engine;

    @Autowired
    private PluginManager pluginManager;

    private Logger log = LogManager.getLogger(this.getClass().getSimpleName());

    protected Logger getLog() {
        return this.log;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected String getHandledCommand() {
        return "pull";
    }

    @Override
    public Worker doHandleCommand(Command command) throws DataFordelerException {
        this.getLog().info("Handling command '"+command.getCommandName()+"'");

        PullCommandData commandData = this.getCommandData(command);
        Plugin plugin = this.getPlugin(commandData);
        this.getLog().info("Pulling with plugin "+plugin.getClass().getCanonicalName());

        Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                PullCommandHandler.this.getLog().error("Pull failed", ex);
            }
        };

        Pull pull = new Pull(engine, plugin);
        pull.setUncaughtExceptionHandler(exceptionHandler);
        return pull;
    }

    private PullCommandData getCommandData(Command command)
        throws DataStreamException, InvalidClientInputException {
        PullCommandData commandData = null;
        try {
            commandData = this.objectMapper.readValue(command.getCommandBody(), PullCommandData.class);
        } catch (IOException e) {
            this.getLog().error("Unable to parse command data");
            throw new InvalidClientInputException("Unable to parse command data");
        }
        this.getLog().info("Command data parsed");
        return commandData;
    }

    private Plugin getPlugin(PullCommandData commandData) throws PluginNotFoundException {
        Plugin plugin = pluginManager.getPluginByName(commandData.plugin);
        if (plugin == null) {
            this.getLog().error("Couldn't find requested plugin '"+commandData.plugin+"'");
            throw new PluginNotFoundException(commandData.plugin, false);
        }
        return plugin;
    }
}
