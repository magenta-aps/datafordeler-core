package dk.magenta.datafordeler.core.exception;

import java.net.URI;

/**
 * Created by lars on 07-03-17.
 */
public class PluginNotFoundException extends DataFordelerException {
    @Override
    public String getCode() {
        return "datafordeler.plugin.plugin_not_found";
    }

    private String schema;
    private URI uri;

    public PluginNotFoundException(String schema) {
        this.schema = schema;
    }

    public PluginNotFoundException(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        if (this.schema != null) {
            return "Plugin that handles schema " + this.schema + " was not found";
        } else if (this.uri != null) {
            return "Plugin that handles URI " + this.uri.toString() + " was not found";
        } else {
            return "Plugin lookup on null";
        }
    }
}
