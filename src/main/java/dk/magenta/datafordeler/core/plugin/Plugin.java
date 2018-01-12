package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;

import java.net.URI;
import java.util.Collection;

/**
 * Base class for a plugin. Defines which methods must be implemented.
 * A Plugin to DAFO is a separate JAR, drawing on and subclassing Core classes.
 * Upon compilation, the jar is stored in a special folder (configured by dafo.plugins.folder)
 * and included in the classpath by the Application class.
 */
public abstract class Plugin {

    protected long version = 1L;

    private PluginManager pluginManager;

    public Plugin() {
    }

    public long getVersion() {
        return version;
    }

    /**
     * Get the unique name for the plugin
     * @return
     */
    public abstract String getName();

    /**
     * Get the plugin's RegisterManager implementation
     * @return
     */
    public abstract RegisterManager getRegisterManager();

    /**
     * Returns whether this plugin can handle input of the given schema
     * @param schema
     * @return
     */
    public boolean handlesSchema(String schema) {
        return this.getRegisterManager().handlesSchema(schema);
    }

    /**
     * Get the plugin's EntityManager implementation for the given schema
     * @return
     */
    public final EntityManager getEntityManager(String schema) {
        return this.getRegisterManager().getEntityManager(schema);
    }

    /**
     * Get the plugin's EntityManager implementation for the given URI
     * @return
     */
    public final EntityManager getEntityManager(URI uri) {
        return this.getRegisterManager().getEntityManager(uri);
    }

    /**
     * Get the plugin's ConfigurationManager implementation
     * @return
     */
    public abstract ConfigurationManager getConfigurationManager();

    public final Collection<String> getHandledURISubstrings() {
        return this.getRegisterManager().getHandledURISubstrings();
    }

    public abstract RolesDefinition getRolesDefinition();

    public abstract AreaRestrictionDefinition getAreaRestrictionDefinition();

    public boolean isDemo() {
        return false;
    }


    public final String getServiceOwner() {
        return this.getName().toLowerCase();
    }

    public final void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public final PluginManager getPluginManager() {
        return this.pluginManager;
    }
}
