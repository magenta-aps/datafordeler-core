package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;

/**
 * An exception class that tells the user that they have been denied access to a resource due
 * to missing permissions.
 */
public class AccessDeniedException extends HttpStatusResponseException {

  public AccessDeniedException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }

  public String getCode() {
    return "datafordeler.accessdenied";
  }
}
