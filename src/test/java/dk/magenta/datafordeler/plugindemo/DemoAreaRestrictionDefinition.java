package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.plugin.AreaRestrictionDefinition;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lars on 12-01-17.
 */
public class DemoAreaRestrictionDefinition extends AreaRestrictionDefinition {

    private DemoPlugin plugin;

    @Override
    protected Plugin getPlugin() {
        return this.plugin;
    }

    public DemoAreaRestrictionDefinition(DemoPlugin plugin) {
        this.plugin = plugin;
        AreaRestrictionType cardinalDirections = this.addAreaRestrictionType(
                "Cardinal directions",
                "The four cardinal directions"
        );
        cardinalDirections.addChoice("North", "The north quarter", null);
        cardinalDirections.addChoice("East", "The east quarter", null);
        cardinalDirections.addChoice("West", "The west quarter", null);
        cardinalDirections.addChoice("South", "The south quarter", null);
    }
}

