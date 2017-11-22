package dk.magenta.datafordeler.core.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by lars on 03-04-17.
 */
public abstract class EntityReference<E extends Entity, R extends RegistrationReference> implements Serializable {
    protected UUID objectId;
    protected ArrayList<R> registrationReferences;
    public UUID getObjectId() {
        return objectId;
    }
    public ArrayList<R> getRegistrationReferences() {
        return registrationReferences;
    }
    public abstract Class<E> getEntityClass();
}
