package dk.magenta.datafordeler.core.arearestriction;

import java.awt.geom.Area;
import java.util.HashMap;

/**
 * Describes a specific area that can be used when limiting queries to data in DAFO.
 *
 */
public class AreaRestriction {
  private String name;
  private String description;
  private String sumiffiik;
  private AreaRestrictionType type;

  private static HashMap<String, AreaRestriction> lookupMap = new HashMap<>();

  public AreaRestriction(String name, String description, String sumiffiik,
      AreaRestrictionType type) {
    this.name = name;
    this.description = description;
    this.sumiffiik = sumiffiik;
    this.type = type;
    lookupMap.put(this.lookupName(), this);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSumifiik() {
    return sumiffiik;
  }

  public AreaRestrictionType getType() {
    return type;
  }

  public String lookupName() {
    return this.type.lookupName() + ":" + this.getName();
  }

  public static AreaRestriction lookup(String lookupName) {
    return lookupMap.get(lookupName);
  }

}
