package dk.magenta.datafordeler.core.exception;

/**
 * Created by jubk on 12-06-2017.
 */
public class AccessRequiredException extends DataFordelerException {

  public AccessRequiredException(String message) {
    super(message);
  }

  @Override
  public String getCode() {
    return "datafordeler.accessrequired";
  }

}
