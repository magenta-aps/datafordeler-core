package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by lars on 29-05-17.
 */
@Component
public abstract class CommandHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(this.getClass().getSimpleName());

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected abstract String getHandledCommand();

    public List<String> getHandledCommands() {
        return Collections.singletonList(this.getHandledCommand());
    }

    public abstract Worker doHandleCommand(Command command) throws DataFordelerException;

    protected Logger getLog() {
        return this.log;
    }

    public String getCommandStatus(Command command) {
        try {
            return this.objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveCommand(Command command) {
        Session session = this.sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(command);
        transaction.commit();
        session.close();
    }
}
