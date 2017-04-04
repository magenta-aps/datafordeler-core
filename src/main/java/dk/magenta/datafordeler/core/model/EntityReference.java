package dk.magenta.datafordeler.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lars on 03-04-17.
 */
public abstract class EntityReference<E extends Entity, R extends RegistrationReference> implements Serializable {
    protected String objectId;
    protected ArrayList<R> registrations;

    public String getObjectId() {
        return objectId;
    }

    public ArrayList<R> getRegistrations() {
        return registrations;
    }
}
