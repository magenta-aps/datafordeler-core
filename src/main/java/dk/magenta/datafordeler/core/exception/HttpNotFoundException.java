package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by jubk on 01-07-2017.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class HttpNotFoundException extends DataFordelerException {

  public HttpNotFoundException(String message) {
    super(message);
  }

  @Override
  public String getCode() {
    return "datafordeler.http.not-found";
  }
}
