package dk.magenta.datafordeler.core.user;

/**
 * Created by jubk on 13-06-2017.
 */
public class AnonymousDafoUserDetails extends DafoUserDetails {

  @Override
  public boolean isAnonymous() {
    return true;
  }

  @Override
  public boolean hasSystemRole(String role) {
    return false;
  }
}
