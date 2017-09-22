package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Dump;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by lars on 29-05-17. A CommandHandler for executing dumps. The
 * command interface
 */
@Component
public class DumpCommandHandler extends CommandHandler {

    public static class DumpCommandData extends CommandData {
        public DumpCommandData() {
        }

        @Override
        public boolean containsAll(Map<String, Object> data) {
            return true;
        }

        @Override
        public Map<String, Object> contents() {
            return Collections.emptyMap();
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
    public Worker doHandleCommand(Command command)
        throws DataFordelerException {
        this.getLog().info("Handling command '{}'", command.getCommandName());

        return new Dump(engine);
    }

    public DumpCommandData getCommandData(String commandBody)
        throws DataStreamException, InvalidClientInputException {
        return null;
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
