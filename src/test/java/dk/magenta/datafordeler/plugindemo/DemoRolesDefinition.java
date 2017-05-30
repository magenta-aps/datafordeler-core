package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.SystemRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lars on 12-01-17.
 */
public class DemoRolesDefinition extends RolesDefinition {
    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<SystemRole>(super.getRoles());

        return roles;
    }
}
