package dk.magenta.datafordeler.plugindemo.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by lars on 06-04-17.
 */
@Component
public class DemoConfigurationManager extends ConfigurationManager<DemoConfiguration> {

    @Autowired
    SessionManager sessionManager;

    private Logger log = LogManager.getLogger("DemoConfigurationManager");

    /**
     * Run bean initialization
     * Load configuration from database
     */
    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<DemoConfiguration> getConfigurationClass() {
        return DemoConfiguration.class;
    }

    @Override
    protected DemoConfiguration createConfiguration() {
        return new DemoConfiguration();
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

}
