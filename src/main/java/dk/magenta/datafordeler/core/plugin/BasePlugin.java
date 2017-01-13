package dk.magenta.datafordeler.core.plugin;

import java.util.List;

/**
 * Created by lars on 11-01-17.
 */

public abstract class BasePlugin {

    protected long version = 1L;

    protected List<Class> entityClasses;

    protected BaseRegisterHandler registerHandler;

    protected BaseRolesDefinition roleDefinition;

    protected BaseFieldsDefinition fieldsDefinition;

    public BasePlugin() {
    }

    public long getVersion() {
        return version;
    }
}
