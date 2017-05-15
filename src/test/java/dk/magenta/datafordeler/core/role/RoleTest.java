package dk.magenta.datafordeler.core.role;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.demoplugin.TestRolesDefinition;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
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
public class RoleTest {

    @Test
    public void testEqual() {
        SystemRole role = new SystemRole();
    }

    @Test
    public void testGetRoles() {
        TestRolesDefinition rolesDefinition = new TestRolesDefinition();
        List<SystemRole> roles = rolesDefinition.getRoles();
    }

}
