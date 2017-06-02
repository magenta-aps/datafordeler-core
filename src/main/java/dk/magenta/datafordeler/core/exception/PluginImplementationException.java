package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 23-05-17.
 */
public class PluginImplementationException extends DataFordelerException {

    public PluginImplementationException(String message) {
        super(message);
    }

    public PluginImplementationException(String messsage, Throwable cause) {
        super(messsage, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.plugin.implementation_error";
    }
}
