package dk.magenta.datafordeler.core.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * An exception class that ensures that the exception message is output to the client along
 * with the correct HTTP Status code.
 */
public abstract class HttpStatusResponseException extends WebApplicationException {

  public HttpStatusResponseException(String message, int status) {
    super(
        Response.status(Response.Status.fromStatusCode(status))
            .entity(message)
            .type(MediaType.TEXT_PLAIN_VALUE)
            .build()
    );
  }

  public HttpStatusResponseException(String message, HttpStatus httpStatus) {
    this(message, httpStatus.value());
  }

  public HttpStatusResponseException(String message, Throwable cause, int status) {
    super(
        cause,
        Response.status(Response.Status.fromStatusCode(status))
            .entity(message)
            .type(MediaType.TEXT_PLAIN_VALUE)
            .build()
    );
  }

  public HttpStatusResponseException(String message, Throwable cause, HttpStatus httpStatus) {
    this(message, cause, httpStatus.value());
  }

}
