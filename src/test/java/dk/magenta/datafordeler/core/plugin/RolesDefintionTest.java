package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by lars on 08-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class RolesDefintionTest extends PluginTestBase {

    @Test
    public void testGetRoles() {
        //RolesDefinition rolesDefinition = this.plugin.getRolesDefinition();
        //List<SystemRole> roles = rolesDefinition.getRoles();
    }

}
