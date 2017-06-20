package dk.magenta.datafordeler.core.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jubk on 20-06-2017.
 */
public class UserProfile {
  private String name;
  private HashSet<String> systemRoles = new HashSet<>();
  private HashSet<AreaRestriction> areaRestrictions = new HashSet<>();

  public UserProfile(String name) {
    this(name, null, null);
  }

  public UserProfile(String name, List<String> systemRoles) {
    this(name, systemRoles, null);
  }

  public UserProfile(
      String name, List<String> systemRoles, List<AreaRestriction> areaRestrictions
  ) {
    this.name = name;
    if(systemRoles != null) {
      this.systemRoles.addAll(systemRoles);
    }
    if(areaRestrictions != null) {
      this.areaRestrictions.addAll(areaRestrictions);
    }
  }

  public String getName() {
    return name;
  }

  public HashSet<String> getSystemRoles() {
    return systemRoles;
  }

  public HashSet<AreaRestriction> getAreaRestrictions() {
    return areaRestrictions;
  }
}
