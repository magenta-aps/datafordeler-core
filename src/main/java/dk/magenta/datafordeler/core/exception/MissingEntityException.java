package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

/**
 * Created by lars on 17-05-17.
 */
public class MissingEntityException extends DataFordelerException {

    private Registration registration;

    public MissingEntityException(Registration registration) {
        this.registration = registration;
    }
    @Override
    public String getCode() {
        return null;
    }
}
