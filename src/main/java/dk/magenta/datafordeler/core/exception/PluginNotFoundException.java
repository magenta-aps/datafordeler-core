package dk.magenta.datafordeler.core.exception;

/**
 * Created by lars on 07-03-17.
 */
public class PluginNotFoundException extends DataFordelerException {
    @Override
    public String getCode() {
        return "datafordeler.plugin.plugin_not_found";
    }

    private String schema;

    public PluginNotFoundException(String schema) {
        this.schema = schema;
    }

    @Override
    public String getMessage() {
        return "Plugin that handles schema "+this.schema+" was not found";
    }
}
