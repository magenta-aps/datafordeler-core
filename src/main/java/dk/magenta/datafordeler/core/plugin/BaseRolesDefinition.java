package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.role.SystemRole;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lars on 11-01-17.
 */
public abstract class BaseRolesDefinition {

  public List<SystemRole> getRoles() {
    return new ArrayList<SystemRole>();
  }

}
