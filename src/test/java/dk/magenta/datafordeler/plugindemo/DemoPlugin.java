package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfigurationManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by lars on 12-01-17.
 */
@Component
public class DemoPlugin extends Plugin {

    @Autowired
    private DemoRegisterManager demoRegisterManager;

    @Autowired
    private DemoEntityManager demoEntityManager;

    @Autowired
    private DemoConfigurationManager demoConfigurationManager;

    public DemoPlugin() {
        this.rolesDefinition = new DemoRolesDefinition();
    }

    @Override
    public String getName() {
        return "Demo";
    }

    @Override
    public RegisterManager getRegisterManager() {
        return this.demoRegisterManager;
    }

    @PostConstruct
    public void init() {
        this.demoRegisterManager.addEntityManager(this.demoEntityManager, DemoEntity.schema);
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return this.demoConfigurationManager;
    }

    public boolean isDemo() {
        return true;
    }

}
