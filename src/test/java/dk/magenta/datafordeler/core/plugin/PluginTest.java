package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by lars on 15-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = TestConfig.class)
public class PluginTest extends PluginTestBase {

    @Test
    public void testGetVersion() {
        Assert.assertEquals(1L, this.plugin.getVersion());
    }

    @Test
    public void testGetEntityManager() throws URISyntaxException {
        URI uri = new URI("http://localhost:" + TestConfig.servicePort);
        Assert.assertTrue(this.plugin.getRegisterManager() instanceof RegisterManager);
        Assert.assertEquals(this.plugin.getEntityManager(DemoEntity.schema), this.plugin.getRegisterManager().getEntityManager(DemoEntity.class));
        Assert.assertEquals(this.plugin.getEntityManager(uri), this.plugin.getEntityManager(DemoEntity.schema));
    }


    @Test
    public void testHandlesSchema() throws URISyntaxException {
        Assert.assertTrue(this.plugin.handlesSchema(DemoEntity.schema));
        Assert.assertFalse(this.plugin.handlesSchema("foobar"));
    }


    @Test
    public void testGetRolesDefinition() throws URISyntaxException {
        Assert.assertTrue(this.plugin.getRolesDefinition() instanceof DemoRolesDefinition);
    }
}
