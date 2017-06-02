package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.plugin.Plugin;

import java.util.regex.Pattern;

/**
 * Created by lars on 20-04-17.
 */
public class InvalidServiceOwnerDefinitionException extends InvalidPluginDefinitionException {
    public InvalidServiceOwnerDefinitionException(Plugin plugin, String ownerDefinition, Pattern validationRegex) {
        super(plugin, "\"" + ownerDefinition + "\" is not a valid owner definition, must conform to regex /" + validationRegex.pattern() + "/");
    }

    @Override
    public String getCode() {
        return "datafordeler.plugin.invalid_owner_definition";
    }
}
