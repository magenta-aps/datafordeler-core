package dk.magenta.datafordeler.core.arearestriction;

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

}
