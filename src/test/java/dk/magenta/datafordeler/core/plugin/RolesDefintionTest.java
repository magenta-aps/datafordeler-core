package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.role.SystemRole;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by lars on 08-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class RolesDefintionTest extends PluginTestBase {

    @Test
    public void testGetRoles() {
        //RolesDefinition rolesDefinition = this.plugin.getRolesDefinition();
        //List<SystemRole> roles = rolesDefinition.getRoles();
    }

}
