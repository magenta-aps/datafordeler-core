package dk.magenta.datafordeler.core.io;

import java.io.Serializable;

public interface PluginSourceData extends Serializable {
    public String getData();
    public String getSchema();
}
