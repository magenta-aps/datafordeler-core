package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by jubk on 15-06-2017.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends DataFordelerException {

  public InvalidTokenException(String message) {
    super(message);
  }

  @Override
  public String getCode() {
    return "datafordeler.authorization.invalid_token";
  }
}
