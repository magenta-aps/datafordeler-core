package dk.magenta.datafordeler.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;

/**
 * Created by jubk on 13-06-2017.
 */
public class SamlDafoUserDetails extends DafoUserDetails {

  private HashMap<String, UserProfile> userProfiles = new HashMap<>();
  private HashMap<String, Collection<UserProfile>> systemRoles = new HashMap<>();

  private NameID nameID;
  private Assertion sourceAssertion;

  private String nameQualifier;
  private String identity;

  public SamlDafoUserDetails(Assertion assertion) {
    super();

    this.sourceAssertion = assertion;

    this.nameQualifier = assertion.getSubject().getNameID().getNameQualifier();
    this.identity = assertion.getSubject().getNameID().getValue();
  }

  public void addUserProfile(UserProfile userprofile) {
    this.userProfiles.put(userprofile.getName(), userprofile);
    for(String systemRole : userprofile.getSystemRoles()) {
      if(systemRoles.containsKey(systemRole)) {
        systemRoles.get(systemRole).add(userprofile);
      } else {
        systemRoles.put(systemRole, Collections.singletonList(userprofile));
      }
    }
  }

  public List<String> getAssertionUserProfileNames() {
    return new ArrayList<>();
  }

  @Override
  public String getNameQualifier() {
    return this.nameQualifier;
  }

  @Override
  public String getIdentity() {
    return this.identity;
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  public boolean hasSystemRole(String role) {
    return systemRoles.containsKey(role);
  }

  @Override
  public boolean hasUserProfile(String userProfileName) {
    return userProfiles.containsKey(userProfileName);
  }

  @Override
  public Collection<String> getUserProfiles() {
    return userProfiles.keySet();
  }

  @Override
  public Collection<String> getSystemRoles() {
    return systemRoles.keySet();
  }

  @Override
  public Collection<UserProfile> getUserProfilesForRole(String role) {
    if(systemRoles.containsKey(role)) {
      return systemRoles.get(role);
    } else {
      return Collections.EMPTY_LIST;
    }
  }
}
