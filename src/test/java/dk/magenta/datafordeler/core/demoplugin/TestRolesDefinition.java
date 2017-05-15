package dk.magenta.datafordeler.core.demoplugin;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.SystemRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lars on 15-05-17.
 */
public class TestRolesDefinition extends RolesDefinition {
    public List<SystemRole> getRoles() {
        return new ArrayList<SystemRole>(super.getRoles());
    }
}
