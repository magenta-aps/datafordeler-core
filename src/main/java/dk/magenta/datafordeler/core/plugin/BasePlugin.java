package dk.magenta.datafordeler.core.plugin;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by lars on 11-01-17.
 */
public abstract class BasePlugin {

    protected long version = 1L;

    List<Class> entityClasses;

    @Autowired
    private BaseRegisterHandler registerHandler;

    @Autowired
    private BaseRolesDefinition roleDefinition;

    @Autowired
    private BaseFieldsDefinition fieldsDefinition;

    BasePlugin() {
    }

    public long getVersion() {
        return version;
    }
}
