package dk.magenta.datafordeler.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by lars on 20-04-17.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidClientInputException extends DataFordelerException {
    public InvalidClientInputException(String message) {
        super(message);
    }

    public InvalidClientInputException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.http.invalid-client-input";
    }
}
