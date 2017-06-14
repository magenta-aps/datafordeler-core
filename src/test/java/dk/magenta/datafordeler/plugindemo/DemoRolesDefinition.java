package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.ReadEntityRole;
import dk.magenta.datafordeler.core.role.ReadEntityRoleVersion;
import dk.magenta.datafordeler.core.role.ReadServiceRole;
import dk.magenta.datafordeler.core.role.ReadServiceRoleVersion;
import dk.magenta.datafordeler.core.role.SystemRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lars on 12-01-17.
 */
public class DemoRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_SERVICE_ROLE = new ReadServiceRole(
        "DemoService",
        new ReadServiceRoleVersion(1.0f, "Initial version")
    );

    public static ReadEntityRole READ_DEMO_ENTITY_ROLE = new ReadEntityRole(
        "DemoEntity",
        READ_SERVICE_ROLE,
        new ReadEntityRoleVersion(1.0f, "Initial version")
    );

    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();

        roles.add(READ_SERVICE_ROLE);
        roles.add(READ_DEMO_ENTITY_ROLE);

        return roles;
    }
}
