package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.command.CommandWatcher;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.role.CommandRole;
import dk.magenta.datafordeler.core.role.ExecuteCommandRole;
import dk.magenta.datafordeler.core.role.SystemRole;
import dk.magenta.datafordeler.core.role.SystemRoleType;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import dk.magenta.datafordeler.core.PluginManager;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by lars on 29-05-17.
 */
@RequestMapping("/command")
@Controller
public class CommandService {

    private Logger log = LoggerFactory.getLogger("CommandService");

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandWatcher commandWatcher;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    protected void checkAndLogAccess(LoggerHelper loggerHelper, SystemRole requiredRole)
            throws AccessDeniedException, AccessRequiredException {
        try {
            this.checkAccess(loggerHelper.getUser(), requiredRole);
        }
        catch(AccessDeniedException|AccessRequiredException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }


    /**
     * Checks that the user has access to the service
     * @param dafoUserDetails DafoUserDetails object representing the user provided from a SAML token.
     * @throws AccessDeniedException
     *
     * Implementing this method as a noop will make the service publicly accessible.
     */
    protected void checkAccess(DafoUserDetails dafoUserDetails, SystemRole requiredRole)
            throws AccessDeniedException, AccessRequiredException {
        dafoUserDetails.checkHasSystemRole(requiredRole);
    }

    private SystemRole findMatchingRole(SystemRoleType requiredAccess, String commandName, CommandData commandData) {
        for (Plugin plugin : pluginManager.getPlugins()) {
            List<SystemRole> pluginRoles = plugin.getRolesDefinition().getRoles();
            if (pluginRoles != null) {
                for (SystemRole role : pluginRoles) {
                    // To gain access, the user must have the defined role that matches all of the following:
                    // * is an ExecuteCommandRole
                    if (role instanceof CommandRole) {
                        CommandRole commandRole = (CommandRole) role;
                        if (
                            // * has a type that matches the type of access requested
                                commandRole.getType() == requiredAccess &&
                                        // * has a target that matches the command requested (e.g. "pull")
                                        commandRole.getCommandName().equalsIgnoreCase(commandName) &&
                                        // * has a details map that validates the command body
                                        commandData.containsAll(commandRole.getDetails())
                                ) {
                            return commandRole;
                        }
                    }
                }
            }
        }
        // If no such role is found, we must block all access
        return null;
    }

    private void checkRole(Command command, CommandHandler handler, SystemRoleType roleType, LoggerHelper loggerHelper) throws DataStreamException, InvalidClientInputException, AccessDeniedException, AccessRequiredException {
        // Check that the CommandHandler can handle the command, and that there exists a SystemRole granting access to the command
        CommandData commandData = handler.getCommandData(command);
        SystemRole requiredRole = this.findMatchingRole(roleType, command.getCommandName(), commandData);
        if (requiredRole == null) {
            loggerHelper.info("No Command Role exists for [SystemRoleType:"+roleType.name()+", Command: "+command.getCommandName()+", CommandData: "+commandData+"]");
            throw new AccessDeniedException("No Command Role exists for command '"+command.getCommandName()+"' with data '"+commandData+"'");
        }
        // Check that the user has this SystemRole
        this.checkAndLogAccess(loggerHelper, requiredRole);
    }

    @RequestMapping(method = RequestMethod.GET, path="{id}")
    public void doGet(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long commandId)
            throws IOException, HttpNotFoundException, InvalidClientInputException, InvalidTokenException, AccessRequiredException, AccessDeniedException, DataStreamException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("GET request received on address " + request.getServletPath());
        if (commandId >= 0) {
            loggerHelper.info("Request for status on job id "+commandId);
            Command command = this.getCommand(commandId);
            if (command == null) {
                throw new HttpNotFoundException("Job id "+commandId+" not found");
            }
            CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
            if (handler == null) {
                loggerHelper.info("No handler found for command "+command.getCommandName()+" (job id "+commandId+")");
                throw new InvalidClientInputException("No handler found for command");
            } else {
                this.checkRole(command, handler, SystemRoleType.ReadCommandRole, loggerHelper);
                String output = handler.getCommandStatus(command);
                loggerHelper.info("Status on job id "+commandId+" is "+output);
                response.getWriter().write(output);
            }
        } else {
            loggerHelper.info("Request for status on job id "+commandId+", but no such job exists");
            throw new HttpNotFoundException("Job id "+commandId+" not found");
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{command}")
    public void doPost(HttpServletRequest request, HttpServletResponse response, @PathVariable("command") String commandName)
            throws IOException, InvalidClientInputException, InvalidTokenException, AccessDeniedException, AccessRequiredException, DataStreamException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("POST request received on address " + request.getServletPath());
        loggerHelper.info("Request for command '"+commandName+"'");
        Command command;
        try {
            // Extract Command object from request
            command = Command.fromRequest(request, commandName);
        } catch (IOException e) {
            loggerHelper.error("Error reading command body", e);
            throw new InvalidClientInputException("Cannot read command body");
        }
        // Ensure that a CommandHandler exists for this command
        CommandHandler handler = commandWatcher.getHandler(command.getCommandName());
        if (handler == null) {
            throw new InvalidClientInputException("No handler found for command '"+commandName+"'");
        } else {
            this.checkRole(command, handler, SystemRoleType.ExecuteCommandRole, loggerHelper);
            // Put the command in the Database with the "queued" status. The CommandWatcher will pick it up
            command.setStatus(Command.Status.QUEUED);
            this.saveCommand(command);
            String output = this.objectMapper.writeValueAsString(command);
            loggerHelper.info("Command queued: "+output);
            response.getWriter().write(output);
        }
        loggerHelper.info("Request complete");
    }

    @RequestMapping(method = RequestMethod.DELETE, path="{id}")
    public void doDelete(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long commandId)
            throws IOException, InvalidClientInputException, HttpNotFoundException, InvalidTokenException, DataStreamException, AccessDeniedException, AccessRequiredException {
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);
        loggerHelper.info("DELETE request received on address " + request.getServletPath());
        if (commandId >= 0) {
            loggerHelper.info("Request for cancelling of job id '"+commandId+"'");
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
                this.checkRole(command, handler, SystemRoleType.StopCommandRole, loggerHelper);
                // Cancel the command
                commandWatcher.cancelCommand(command);
                String output = this.objectMapper.writeValueAsString(command);
                loggerHelper.info("Status on job id "+commandId+" is "+output);
                response.getWriter().write(output);
            }
        } else {
            loggerHelper.info("Request for cancelling job id "+commandId+", but no such job exists");
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
