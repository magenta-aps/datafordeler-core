package dk.magenta.datafordeler.core.role;

import java.util.Map;

public abstract class CommandRole extends SystemRole {

    private String commandName;
    private Map<String, Object> details;

    public CommandRole(SystemRoleType type, SystemRoleGrant grantType, String commandName, Map<String, Object> details, SystemRole parent, SystemRoleVersion... versions) {
        super(type, grantType, parent, versions);
        this.commandName = commandName;
        this.details = details;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public Map<String, Object> getDetails() {
        return this.details;
    }

    @Override
    public String getTargetName() {
        return this.getCommandName() + this.getDetails();
    }
}
