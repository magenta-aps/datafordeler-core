package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.io.PluginSourceData;

/**
 * Created by lars on 07-03-17.
 */
public class MissingReferenceException extends DataFordelerException {

    private PluginSourceData event;

    public MissingReferenceException(PluginSourceData event) {
        super();
        this.event = event;
    }

    public PluginSourceData getEvent() {
        return event;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.missing_reference";
    }
}
