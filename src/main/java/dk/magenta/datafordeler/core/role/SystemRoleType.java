package dk.magenta.datafordeler.core.role;

/**
 * Enumerates the different types of SystemRoles that are possible in DAFO. If changes are made
 * here the same changes should be made in DAFO admin's models.py.
 */
public enum SystemRoleType {
  /* A role that gives access to a full service, eg. everything CPR related */
  ServiceRole(1),
  /* A role that gives access to all data for a specific entity, eg. a Person */
  EntityRole(2),
  /* A role that gives access to a specific attribute on an entity, eg. the Cpr-nr on a Person */
  AttributeRole(3),

  /* A custom role type used non-data specific access */
  CustomRole(4),

  /* A role that gives access to execute, read status of, and stop a specific command, e.g. PULL */
  CommandRole(5);


  private int numericValue;

  SystemRoleType(int numericValue) {
    this.numericValue = numericValue;
  }

  public int getNumericValue() {
    return numericValue;
  }
}