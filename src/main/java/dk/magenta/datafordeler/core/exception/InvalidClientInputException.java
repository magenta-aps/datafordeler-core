package dk.magenta.datafordeler.core.exception;

import java.util.zip.DataFormatException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @Override
    public String getCode() {
        return "datafordeler.http.invalid-client-input";
    }
}
