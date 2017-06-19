package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;

/**
 * An exception class that represents an error in processing an incoming SAML token.
 */
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
