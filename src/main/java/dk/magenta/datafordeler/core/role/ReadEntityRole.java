package dk.magenta.datafordeler.core.role;

/**
 * Created by jubk on 16-03-2017.
 */
public class ReadEntityRole extends SystemRole{

  private String entityName;

  protected ReadEntityRole(
      String entityName, ReadServiceRole parent, ReadEntityRoleVersion... versions
  ) {
    super(SystemRoleType.EntityRole, SystemRoleGrant.Read, parent, versions);
    this.entityName = entityName;
  }

  @Override
  public String getTargetName() {
    return this.getParent().getTargetName() + entityName;
  }
}
