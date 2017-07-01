package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception class that tells the user that they have been denied access to a resource due
 * to missing permissions.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends HttpStatusResponseException {

  public AccessDeniedException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }

  public String getCode() {
    return "datafordeler.accessdenied";
  }
}
