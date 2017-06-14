package dk.magenta.datafordeler.core.exception;

/**
 * Created by jubk on 12-06-2017.
 */
public class AccessDeniedException extends DataFordelerException {

  public AccessDeniedException(String message) {
    super(message);
  }

  @Override
  public String getCode() {
    return "datafordeler.accessdenied";
  }
}
