package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfigurationManager;
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
        AreaRestrictionType cardinalDirections = this.addAreaRestrictionType(
            "Cardinal directions",
            "The four cardinal directions"
        );

        cardinalDirections.addChoice("North", "The north quarter", null);
        cardinalDirections.addChoice("East", "The east quarter", null);
        cardinalDirections.addChoice("West", "The west quarter", null);
        cardinalDirections.addChoice("South", "The south quarter", null);
    }

    @Override
    public String getName() {
        return "Demo";
    }

    @Override
    public RegisterManager getRegisterManager() {
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

}
