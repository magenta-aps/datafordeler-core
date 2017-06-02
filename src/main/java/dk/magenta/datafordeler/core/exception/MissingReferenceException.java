package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.io.Event;

/**
 * Created by lars on 07-03-17.
 */
public class MissingReferenceException extends DataFordelerException {

    private Event event;

    public MissingReferenceException(Event event) {
        super();
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.missing_reference";
    }
}
