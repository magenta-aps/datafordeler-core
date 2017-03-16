package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.event.EntityManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by lars on 11-01-17.
 */

public abstract class Plugin {

    protected long version = 1L;

    protected Map<String, EntityManager> entityManagers;

    protected RegisterManager registerManager;

    protected RolesDefinition roleDefinition;

    protected FieldsDefinition fieldsDefinition;

    protected HashSet<String> handledSchemas;

    public Plugin() {
        this.entityManagers = new HashMap<>();
        this.handledSchemas = new HashSet<>();
    }

    public long getVersion() {
    return version;
    }
/*
    public void processBusinessEvent(BusinessEvent event) {
        this.registerHandler.processBusinessEvent(event);
    }

    public void processDataEvent(DataEvent event) {
        this.registerHandler.processDataEvent(event);
    }*/

    public EntityManager getEntityManager(String schema) {
        return this.entityManagers.get(schema);
    }

    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    public boolean handlesSchema(String schema) {
        return this.handledSchemas.contains(schema);
    }
}
