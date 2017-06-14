package dk.magenta.datafordeler.core.role;

import org.junit.Assert;
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
public class RoleTest {

    @Test
    public void testEqual() {
        SystemRole role = new ReadServiceRole(
            "MyService",
            new ReadServiceRoleVersion(0.1f, "For testing only")
        );

        Assert.assertEquals("ReadMyService", role.getRoleName());
    }
}
