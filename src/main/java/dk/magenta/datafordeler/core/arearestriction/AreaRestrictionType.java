package dk.magenta.datafordeler.core.arearestriction;

import dk.magenta.datafordeler.core.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a type of area restrictions used by a given service or plugin.
 */
public class AreaRestrictionType {

    private String name;
    private String description;
    private Plugin sourcePlugin;

    private List<AreaRestriction> choices = new ArrayList<>();

    public AreaRestrictionType(String name, String description, Plugin sourcePlugin) {
        this.name = name;
        this.description = description;
        this.sourcePlugin = sourcePlugin;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getServiceName() {
        return sourcePlugin.getName();
    }

    public AreaRestriction addChoice(String name, String description, String sumiffiik) {
        AreaRestriction areaRestriction = new AreaRestriction(name, description, sumiffiik, this);
        choices.add(areaRestriction);
        return areaRestriction;
    }

    public List<AreaRestriction> getChoices() {
        return choices;
    }

    public String lookupName() {
        return this.getServiceName() + ":" + this.getName();
    }
}
