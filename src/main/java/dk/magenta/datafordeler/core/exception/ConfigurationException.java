package dk.magenta.datafordeler.core.exception;

public class ConfigurationException extends DataFordelerException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getCode() {
        return "datafordeler.configuration";
    }
}
