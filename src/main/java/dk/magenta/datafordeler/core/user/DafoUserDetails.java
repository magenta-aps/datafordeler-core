package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.role.SystemRole;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by jubk on 12-06-2017.
 */
public abstract class DafoUserDetails {

  public abstract boolean hasSystemRole(String role);

  public abstract boolean isAnonymous();

  public boolean hasSystemRole(SystemRole role) {
    return hasSystemRole(role.getRoleName());
  }

  public void checkHasSystemRole(String role) throws AccessDeniedException {
    if(!hasSystemRole(role)) {
      throw new AccessDeniedException(
          "User " + this.toString() + " does not have access to " + role
      );
    }
  }

  public void checkHasSystemRole(SystemRole role) throws AccessDeniedException {
    checkHasSystemRole(role.getRoleName());
  }

}
