package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.role.SystemRole;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lars on 11-01-17.
 * Container for SystemRoles in a plugin. Plugins need to return one of these in
 * the getRolesDefinition method, and define relevant roles in the subclass
 */
public abstract class RolesDefinition {

    /**
     * @return Defined roles for the plugin
     */
    public List<SystemRole> getRoles() {
        return new ArrayList<SystemRole>();
    }

}
