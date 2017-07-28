package dk.magenta.datafordeler.core.role;

import java.util.Map;

public class StopCommandRole extends SystemRole {

    private String commandName;
    private Map<String, Object> details;
    // TODO: store and check command body

    public StopCommandRole(String commandName, Map<String, Object> details, SystemRoleVersion... versions) {
        super(SystemRoleType.StopCommandRole, SystemRoleGrant.Stop, null, versions);
        this.commandName = commandName;
        this.details = details;
    }

    @Override
    public String getTargetName() {
        return commandName + this.details;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public Map<String, Object> getDetails() {
        return this.details;
    }
}
