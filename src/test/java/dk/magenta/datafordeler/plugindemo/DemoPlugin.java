package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
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

    private DemoRolesDefinition rolesDefinition = new DemoRolesDefinition();

    public DemoPlugin() {

    }

    @Override
    public String getName() {
        return "Demo";
    }

    @Override
    public RegisterManager getRegisterManager() {
        System.out.println("===== DemoPlugin " + DemoPlugin.class.getClassLoader());
        return this.demoRegisterManager;
    }

    /**
     * Run bean initialization
     * Couple DemoEntityManager to DemoRegisterManager
     */
    @PostConstruct
    public void init() {
        this.demoRegisterManager.addEntityManager(this.demoEntityManager);
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return this.demoConfigurationManager;
    }

    public boolean isDemo() {
        return true;
    }

    @Override
    public RolesDefinition getRolesDefinition() {
        return this.rolesDefinition;
    }
}
