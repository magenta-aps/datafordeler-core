package dk.magenta.datafordeler.core.exception;

import java.net.URI;

/**
 * Created by lars on 07-03-17.
 */
public class EntityManagerNotFoundException extends DataFordelerException {
    @Override
    public String getCode() {
        return "datafordeler.plugin.entitymanager_not_found";
    }

    private String schema;
    private URI uri;

    public EntityManagerNotFoundException(String schema) {
        this.schema = schema;
    }

    public EntityManagerNotFoundException(URI uri) {
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        if (this.schema != null) {
            return "EntityManager that handles schema " + this.schema + " was not found";
        } else if (this.uri != null) {
            return "EntityManager that handles URI " + this.uri.toString() + " was not found";
        } else {
            return "EntityManager lookup on null";
        }
    }
}
