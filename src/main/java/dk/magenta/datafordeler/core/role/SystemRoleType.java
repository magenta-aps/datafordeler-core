package dk.magenta.datafordeler.core.role;

/**
 * Created by jubk on 16-03-2017.
 */
public enum SystemRoleType {
  /* A role that gives access to a full service, eg. everything CPR related */
  ServiceRole,
  /* A role that gives access to all data for a specific entity, eg. a Person */
  EntityRole,
  /* A role that gives access to a specific attribute on an entity, eg. the Cpr-nr on a Person */
  AttributeRole
}