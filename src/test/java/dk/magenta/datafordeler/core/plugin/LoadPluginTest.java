package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.plugindemo.DemoEntityManager;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfiguration;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistrationReference;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


/**
 * Created by lars on 12-01-17.
 */
@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
public class LoadPluginTest {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    SessionManager sessionManager;

    @Test
    @Order(order=1)
    public void listDemoPlugin() {
        List<Plugin> plugins = pluginManager.getPlugins();
        Assert.assertNotNull(plugins);
        Assert.assertTrue("There should be loaded at least one plugin ("+plugins.size()+" loaded)", plugins.size() > 0);
        int foundDemo = 0;
        for (Plugin plugin : plugins) {
            if (plugin.getClass().getName().equals("dk.magenta.datafordeler.plugindemo.DemoPlugin")) {
                foundDemo++;
            }
        }
        Assert.assertTrue("The demo plugin should be loaded exactly once (loaded "+foundDemo+")", foundDemo == 1);
    }

    @Test
    @Order(order=2)
    public void failPluginTest() {
        Assert.assertNull(pluginManager.getPluginForSchema("foobar"));
    }


    @Test
    @Order(order=3)
    public void findDemoPluginTest1() {
        String testSchema = DemoEntity.schema;
        Plugin foundPlugin = this.pluginManager.getPluginForSchema(testSchema);
        Assert.assertEquals(DemoPlugin.class, foundPlugin.getClass());

        EntityManager foundEntityManager = foundPlugin.getEntityManager(testSchema);
        Assert.assertEquals(DemoEntityManager.class, foundEntityManager.getClass());

        Assert.assertEquals(DemoEntity.class, foundEntityManager.getManagedEntityClass());
        Assert.assertEquals(DemoRegistration.class, foundEntityManager.getManagedRegistrationClass());
        Assert.assertEquals(DemoRegistrationReference.class, foundEntityManager.getManagedRegistrationReferenceClass());
    }



    @Test
    @Order(order=4)
    public void findDemoPluginTest2() throws URISyntaxException {
        Plugin foundPlugin = this.pluginManager.getPluginForURI(new URI("http://localhost:" + Application.servicePort));
        Assert.assertNotNull(foundPlugin);
        Assert.assertEquals(DemoPlugin.class, foundPlugin.getClass());


        foundPlugin = this.pluginManager.getPluginForURI(new URI("http://example.com"));
        Assert.assertNull(foundPlugin);
    }

    @Test
    @Order(order=5)
    public void configurationTest() {
        Plugin plugin = this.pluginManager.getPluginForSchema(DemoEntity.schema);
        Session session = sessionManager.getSessionFactory().openSession();
        DemoConfiguration configuration = (DemoConfiguration) plugin.getConfigurationManager().getConfiguration();
        Assert.assertNotNull(configuration.getPullCronSchedule());
        Assert.assertEquals("0 0 0 * * ?", configuration.getPullCronSchedule());
        session.close();
    }



}
