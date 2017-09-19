package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.PluginNotFoundException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.Dump;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by lars on 29-05-17.
 * A CommandHandler for executing dumps. The command interface
 */
@Component
public class DumpCommandHandler extends CommandHandler {

    public static class DumpCommandData extends CommandData {

        private static final String KEY = "query";

        public DumpCommandData() {
        }

        @JsonProperty(required = true)
        public String query;

        @JsonProperty(required = true)
        public String plugin;

        @Override
        public boolean containsAll(Map<String, Object> data) {
            for (String key : data.keySet()) {
                if (key.equals(KEY) && this.query != null &&
                        this.query.equals(data.get(KEY))) {
                    // Ok for now
                } else {
                    // This must not happen. It means there is an important difference between the incoming map and this object
                    return false;
                }
            }
            return true;
        }

        public String getQuery() {
            return this.query;
        }

        @Override
        public Map<String, Object> contents() {
            return Collections.singletonMap(KEY, this.query);
        }
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
        return "dump";
    }

    @Override
    public Worker doHandleCommand(Command command) throws DataFordelerException {
        this.getLog().info("Handling command '"+command.getCommandName()+"'");

        DumpCommandData commandData = this.getCommandData(command.getCommandBody());

        this.getLog().info("Dumping with stuff "+commandData);

        Dump dump = new Dump(engine, commandData);

        return dump;
    }

    public DumpCommandData getCommandData(String commandBody)
            throws DataStreamException, InvalidClientInputException {
        try {
            DumpCommandData commandData = this.objectMapper.readValue(commandBody, DumpCommandData.class);
            this.getLog().info("Command data parsed");
            return commandData;
        } catch (IOException e) {
            this.getLog().error("Unable to parse command data '"+commandBody+"'");
            throw new InvalidClientInputException("Unable to parse command data");
        }
    }

    private Plugin getPlugin(DumpCommandData commandData) throws PluginNotFoundException {
        Plugin plugin = pluginManager.getPluginByName(commandData.query);
        if (plugin == null) {
            this.getLog().error("Couldn't find requested plugin '"+commandData.query+"'");
            throw new PluginNotFoundException(commandData.query, false);
        }
        return plugin;
    }

    public String getCommandStatus(Command command) {
        try {
            return this.objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
