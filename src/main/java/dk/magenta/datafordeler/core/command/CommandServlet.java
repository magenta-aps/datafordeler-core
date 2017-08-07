package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lars on 29-05-17.
 */
@RequestMapping("/command")
@Controller
public class CommandServlet {

    private Logger log = LogManager.getLogger("CommandServlet");

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandWatcher commandWatcher;

    @RequestMapping(method = RequestMethod.GET, path="{id}")
    public void doGet(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long commandId)
        throws IOException, HttpNotFoundException, InvalidClientInputException {
        this.log.info("GET request received on address " + request.getServletPath());
        if (commandId >= 0) {
            this.log.info("Request for status on job id "+commandId);
            Command command = this.getCommand(commandId);
            if (command == null) {
                throw new HttpNotFoundException("Job id "+commandId+" not found");
            }
            CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
            if (handler == null) {
                this.log.info("No handler found for command "+command.getCommandName()+" (job id "+commandId+")");
                throw new InvalidClientInputException("No handler found for command");
            } else {
                String output = handler.getCommandStatus(command);
                this.log.info("Status on job id "+commandId+" is "+output);
                response.getWriter().write(output);
            }
        } else {
            this.log.info("Request for status on job id "+commandId+", but no such job exists");
            throw new HttpNotFoundException("Job id "+commandId+" not found");
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{command}")
    public void doPost(HttpServletRequest request, HttpServletResponse response, @PathVariable("command") String commandName)
        throws IOException, InvalidClientInputException {
        this.log.info("POST request received on address " + request.getServletPath());
        this.log.info("Request for command '"+commandName+"'");
        Command command;
        try {
            command = Command.fromRequest(request, commandName);
        } catch (IOException e) {
            this.log.error("Error reading command body", e);
            throw new InvalidClientInputException("Cannot read command body");
        }
        CommandHandler handler = commandWatcher.getHandler(commandName);
        if (handler == null) {
            throw new InvalidClientInputException("No handler found for command '"+commandName+"'");
        } else {
            command.setStatus(Command.Status.QUEUED);
            this.saveCommand(command);
            String output = this.objectMapper.writeValueAsString(command);
            this.log.info("Command queued: "+output);
            response.getWriter().write(output);
        }
        this.log.info("Request complete");
    }

    @RequestMapping(method = RequestMethod.DELETE, path="{id}")
    public void doDelete(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long commandId)
        throws IOException, InvalidClientInputException, HttpNotFoundException {
        this.log.info("DELETE request received on address " + request.getServletPath());
        if (commandId >= 0) {
            this.log.info("Request for cancelling of job id '"+commandId+"'");
            Command command = this.getCommand(commandId);
            if (command == null) {
                throw new HttpNotFoundException("Command id "+commandId+" not found");
            }
            CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
            if (handler == null) {
                throw new InvalidClientInputException(
                    "No handler found for command '"+command.getCommandName()+"'"
                );
            } else {
                commandWatcher.cancelCommand(command);
                String output = this.objectMapper.writeValueAsString(command);
                this.log.info("Status on job id "+commandId+" is "+output);
                response.getWriter().write(output);
            }
        } else {
            this.log.info("Request for cancelling job id "+commandId+", but no such job exists");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private Command getCommand(long commandId) {
        Session session = sessionManager.getSessionFactory().openSession();
        Command command = null;
        try {
            Query query = session.createQuery("select c from Command c where c.id = :id");
            query.setParameter("id", commandId);
            command = (Command) query.getSingleResult();
        } catch (NoResultException e) {
        }
        session.close();
        return command;
    }

    public synchronized void saveCommand(Command command) {
        Session session = this.sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(command);
        transaction.commit();
        session.close();
    }
}
