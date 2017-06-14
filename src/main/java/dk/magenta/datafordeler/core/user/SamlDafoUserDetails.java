package dk.magenta.datafordeler.core.user;

/**
 * Created by jubk on 13-06-2017.
 */
public class SamlDafoUserDetails extends DafoUserDetails {

  public SamlDafoUserDetails(String substring) {
    // TODO: Parse token
    // TODO: Validate token
    // TODO: Convert token UserProfiles to SystemRoles
    // TODO: Setup areaRestrictions
    super();
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  public boolean hasSystemRole(String role) {
    // TODO: Implement proper lookup
    return false;
  }


}
