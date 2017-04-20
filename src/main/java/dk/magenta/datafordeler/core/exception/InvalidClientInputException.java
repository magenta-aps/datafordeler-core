package dk.magenta.datafordeler.core.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by lars on 20-04-17.
 */
public class InvalidClientInputException extends WebApplicationException {
    public InvalidClientInputException(String message) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
