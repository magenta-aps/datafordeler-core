package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.event.EntityManager;
import dk.magenta.datafordeler.core.model.Registration;

import java.net.URI;
import java.util.*;

/**
 * Created by lars on 11-01-17.
 */
public abstract class Plugin {

    protected long version = 1L;

    protected RegisterManager registerManager;

    protected List<EntityManager> entityManagers;

    protected Map<String, EntityManager> entityManagerBySchema;

    protected Map<String, EntityManager> entityManagerByURISubstring;

    protected RolesDefinition roleDefinition;

    protected FieldsDefinition fieldsDefinition;

    protected HashSet<String> handledSchemas;

    public Plugin() {
        this.handledSchemas = new HashSet<>();
        this.entityManagers = new ArrayList<>();
        this.entityManagerBySchema = new HashMap<>();
        this.entityManagerByURISubstring = new HashMap<>();
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
    }
*/

    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    public EntityManager getEntityManager(String schema) {
        return this.entityManagerBySchema.get(schema);
    }

    public EntityManager getEntityManager(URI uri) {
        String uriString = uri.toString();
        for (String substring : this.entityManagerByURISubstring.keySet()) {
            if (uriString.startsWith(substring)) {
                return this.entityManagerByURISubstring.get(substring);
            }
        }
        return null;
    }


    public boolean handlesSchema(String schema) {
        return this.handledSchemas.contains(schema);
    }

    public abstract Collection<String> getHandledURISubstrings();

    protected void addEntityManager(EntityManager entityManager, String schema) {
        this.handledSchemas.add(schema);
        this.entityManagers.add(entityManager);
        this.entityManagerBySchema.put(schema, entityManager);
        for (String substring : entityManager.getHandledURISubstrings()) {
            this.entityManagerByURISubstring.put(substring, entityManager);
        }
    }

}
