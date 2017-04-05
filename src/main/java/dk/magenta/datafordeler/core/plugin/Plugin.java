package dk.magenta.datafordeler.core.plugin;

import java.net.URI;
import java.util.*;

/**
 * Created by lars on 11-01-17.
 */
public abstract class Plugin {

    protected long version = 1L;

    protected RolesDefinition roleDefinition;

    protected FieldsDefinition fieldsDefinition;

    protected RegisterManager registerManager;

    public Plugin() {
    }

    public long getVersion() {
    return version;
    }

    public abstract Collection<String> getHandledURISubstrings();

    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    public boolean handlesSchema(String schema) {
        return this.registerManager.handlesSchema(schema);
    }

    public EntityManager getEntityManager(String schema) {
        return this.registerManager.getEntityManager(schema);
    }

    public EntityManager getEntityManager(URI uri) {
        return this.registerManager.getEntityManager(uri);
    }
}
