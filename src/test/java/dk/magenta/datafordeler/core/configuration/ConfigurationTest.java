package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.database.SessionManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by lars on 05-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ConfigurationTest {

    @Autowired
    private SessionManager sessionManager;

    private class ConfigurationImpl implements Configuration {
    }

    private class ConfigurationManagerImpl extends ConfigurationManager<ConfigurationImpl> {

        @Override
        protected Class<ConfigurationImpl> getConfigurationClass() {
            return ConfigurationImpl.class;
        }

        @Override
        protected ConfigurationImpl createConfiguration() {
            return new ConfigurationImpl();
        }

        @Override
        protected SessionManager getSessionManager() {
            return ConfigurationTest.this.sessionManager;
        }
    }
    @Test
    public void testConfiguration() throws Exception {
        ConfigurationManagerImpl configurationManager = new ConfigurationManagerImpl();
        ConfigurationImpl configuration = configurationManager.createConfiguration();
        Assert.assertNotNull(configuration);
        Assert.assertEquals(ConfigurationImpl.class, configurationManager.getConfigurationClass());
    }
}
