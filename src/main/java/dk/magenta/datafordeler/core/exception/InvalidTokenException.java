package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception class that represents an error in processing an incoming SAML token.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends HttpStatusResponseException {

  public InvalidTokenException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }

  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause, HttpStatus.UNAUTHORIZED);
  }

  public String getCode() {
    return "datafordeler.authorization.invalid_token";
  }
}
