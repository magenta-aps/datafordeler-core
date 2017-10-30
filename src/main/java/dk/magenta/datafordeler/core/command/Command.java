package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.util.InputStreamReader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

/**
 * Created by lars on 29-05-17.
 * Command descriptor, holding data about an issued command
 */
@Entity
@Table(name = "command", indexes = {@Index(name="status", columnList = "status")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Command extends DatabaseEntry {

    public enum Status {
        QUEUED,
        PROCESSING,
        SUCCESS,
        FAILED,
        CANCELLED
    }

    @Column(nullable = false)
    @JsonIgnore
    private String issuer;

    @Column(nullable = true)
    private OffsetDateTime received;

    @Column(nullable = true)
    private OffsetDateTime handled;

    @Column(nullable = true)
    @JsonIgnore
    private Status status;

    @Column(nullable = true, length = 2048)
    private String errorMessage;

    @Column(nullable = false)
    private String commandName;

    @Column(nullable = true)
    private String commandBody;

    public Command() {}

    public Command(String commandName) {
        this.setCommandName(commandName);
    }

    /**
     * Create a Command object from a http request, a user and a command name
     * The request is used to obtain the command body (ie. the request body), to be parsed by a CommandHandler
     * The user object is used to set the command issuer
     * Also sets the received time
     * @param request
     * @param userDetails
     * @param commandName
     * @return
     * @throws IOException
     */
    public static Command fromRequest(HttpServletRequest request, DafoUserDetails userDetails, String commandName) throws IOException {
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        Command command = new Command(commandName);
        ObjectMapper objectMapper = new ObjectMapper();
        String commandBody = objectMapper.writeValueAsString(
                objectMapper.readTree(
                        request.getInputStream()
                )
        );
        System.out.println("commandBody: "+commandBody);
        //String commandBody = InputStreamReader.readInputStream(requestBody);
        command.setCommandBody(commandBody);
        command.setReceived();
        command.setIssuer(userDetails.getIdentity());
        return command;
    }

    @JsonProperty
    public Long getId() {
        return super.getId();
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public OffsetDateTime getReceived() {
        return received;
    }

    public void setReceived() {
        this.received = OffsetDateTime.now();
    }

    public OffsetDateTime getHandled() {
        return handled;
    }

    public void setHandled() {
        this.handled = OffsetDateTime.now();
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean done() {
        return this.status == Command.Status.SUCCESS || this.status == Command.Status.CANCELLED || this.status == Command.Status.FAILED;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandBody() {
        return commandBody;
    }

    public void setCommandBody(String commandBody) {
        this.commandBody = commandBody;
    }

    @JsonProperty(value = "status")
    public String getStatusName() {
        if (this.status != null) {
            switch (this.status) {
                case QUEUED:
                    return "queued";
                case PROCESSING:
                    return "running";
                case SUCCESS:
                    return "successful";
                case FAILED:
                    return "failure";
                case CANCELLED:
                    return "cancelled";
            }
        }
        return "running";
    }

}
