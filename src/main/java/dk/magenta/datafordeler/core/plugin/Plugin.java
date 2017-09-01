package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;

import java.net.URI;
import java.util.*;

/**
 * Created by lars on 11-01-17.
 */
public abstract class Plugin {

    protected long version = 1L;

    protected List<AreaRestrictionType> areaRestrictionTypes = new ArrayList<>();

    private PluginManager pluginManager;

    public Plugin() {
    }

    public long getVersion() {
        return version;
    }

    public abstract String getName();

    public abstract RegisterManager getRegisterManager();

    public boolean handlesSchema(String schema) {
        return this.getRegisterManager().handlesSchema(schema);
    }

    public EntityManager getEntityManager(String schema) {
        return this.getRegisterManager().getEntityManager(schema);
    }

    public EntityManager getEntityManager(URI uri) {
        return this.getRegisterManager().getEntityManager(uri);
    }

    public abstract ConfigurationManager getConfigurationManager();

    public Collection<String> getHandledURISubstrings() {
        return this.getRegisterManager().getHandledURISubstrings();
    }

    public abstract RolesDefinition getRolesDefinition();

    public boolean isDemo() {
        return false;
    }

    public AreaRestrictionType addAreaRestrictionType(String name, String description) {
        AreaRestrictionType areaRestrictionType = new AreaRestrictionType(
            name, description, this
        );
        this.areaRestrictionTypes.add(areaRestrictionType);

        return areaRestrictionType;
    }

    public List<AreaRestrictionType> getAreaRestrictionTypes() {
        return areaRestrictionTypes;
    }

    public String getServiceOwner() {
        return this.getName().toLowerCase();
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }
}
