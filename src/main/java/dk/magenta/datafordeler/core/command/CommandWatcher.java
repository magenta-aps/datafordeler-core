package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by lars on 06-06-17.
 */
@Component
public class CommandWatcher {

    private Logger log = LogManager.getLogger("CommandWatcher");

    @Autowired
    private SessionManager sessionManager;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private List<CommandHandler> commandHandlers;

    private HashMap<String, CommandHandler> mappedHandlers;

    private HashMap<Long, Worker> workers = new HashMap<>();
    private HashMap<Long, Future> futures = new HashMap<>();

    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /**
     * Run bean initialization
     * Populate handler map for lookup by command name
     */
    @PostConstruct
    public void init() {
        //this.session = this.sessionManager.getSessionFactory().openSession();
        this.mappedHandlers = new HashMap<>();
        for (CommandHandler commandHandler : this.commandHandlers) {
            for (String handledCommand : commandHandler.getHandledCommands()) {
                mappedHandlers.put(handledCommand.toLowerCase(), commandHandler);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        //this.session.close();
    }

    private synchronized List<Command> getCommands() {
        Session session = this.sessionManager.getSessionFactory().openSession();
        Query<Command> query = session.createQuery("select c from Command c where c.status = :status", Command.class);
        query.setParameter("status", Command.Status.QUEUED);
        List<Command> commands = query.getResultList();
        session.close();
        return commands;
    }

    @Scheduled(fixedRate = 2000)
    public void run() {
        List<Command> commands = this.getCommands();
        if (!commands.isEmpty()) {
            this.log.info("Found " + commands.size() + " queued commands");
            for (Command command : commands) {
                CommandHandler commandHandler = this.getHandler(command.getCommandName());
                try {
                    command.setStatus(Command.Status.PROCESSING);
                    this.saveCommand(command);
                    Worker worker = commandHandler.doHandleCommand(command);
                    workers.put(command.getId(), worker);

                    worker.setCallback(new Worker.WorkerCallback() {
                        @Override
                        public void onComplete(boolean cancelled) {
                            super.onComplete(cancelled);
                            if (cancelled) {
                                command.setStatus(Command.Status.CANCELLED);
                            } else {
                                command.setStatus(Command.Status.SUCCESS);
                            }
                            CommandWatcher.this.commandComplete(command);
                        }

                        @Override
                        public void onError(DataFordelerException e) {
                            super.onError(e);
                            command.setStatus(Command.Status.FAILED);
                            CommandWatcher.this.commandComplete(command);
                        }
                    });
                    this.log.info("Worker " + worker.getId() + " obtained, executing");
                    this.futures.put(command.getId(), this.threadPoolExecutor.submit(worker));

                } catch (DataFordelerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void commandComplete(Command command) {
        CommandWatcher.this.saveCommand(command);
        this.workers.remove(command.getId());
    }

    public void cancelCommand(Command command) {
        if (!command.done()) {
            Worker worker = workers.get(command.getId());
            this.log.info("Cancelling worker for command " + command.getId() + " on " + OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            if (worker == null) {
                this.log.info("Didn't find worker");

            } else {
                worker.end();
                try {
                    this.log.info("Waiting for command " + command.getId() + " to end");
                    Future future = this.futures.get(command.getId());
                    future.get();
                    this.log.info("Command ended");
                } catch (InterruptedException | ExecutionException e) {
                    this.log.error(e);
                    e.printStackTrace();
                }
            }
            command.setHandled();
            command.setStatus(Command.Status.CANCELLED);
            this.saveCommand(command);
        }
    }

    public CommandHandler getHandler(String commandName) {
        CommandHandler handler = this.mappedHandlers.get(commandName);
        if (handler == null) {
            this.log.info("No handler found for command.");
            if (this.mappedHandlers.isEmpty()) {
                this.log.info("No handlers registered");
            } else {
                this.log.info("Handlers: ");
                for (String name : this.mappedHandlers.keySet()) {
                    this.log.info("    " + name + " [" + this.mappedHandlers.get(name).getClass().getCanonicalName() + "]");
                }
            }
        }
        return handler;
    }

    public synchronized void saveCommand(Command command) {
        Session session = this.sessionManager.getSessionFactory().openSession();
        command = (Command) session.merge(command);
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(command);
        transaction.commit();
        session.close();
    }
}