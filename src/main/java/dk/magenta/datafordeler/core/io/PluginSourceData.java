package dk.magenta.datafordeler.core.io;

import java.io.Serializable;

/**
 * A generic container definition for incoming data. Plugins should implement
 * this in a class, and use it to envelop data for the Engine.handleEvent() method,
 * and accept it in their EntityManager.parseRegistration() implementations
 */
public interface PluginSourceData extends Serializable {
    public String getData();
    public String getReference();
    public String getSchema();
    public String getId();
}
