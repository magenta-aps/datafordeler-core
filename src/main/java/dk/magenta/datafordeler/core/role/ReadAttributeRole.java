package dk.magenta.datafordeler.core.role;

/**
 * Created by jubk on 16-03-2017.
 */
public class ReadAttributeRole extends SystemRole {
  private String attributeName;

  public ReadAttributeRole(
      String attributeName, ReadEntityRole parent, ReadAttributeRoleVersion... versions

  ) {
    super(SystemRoleType.AttributeRole, SystemRoleGrant.Read, parent, versions);
    this.attributeName = attributeName;
  }

  @Override
  public String getTargetName() {
    return this.getParent().getTargetName() + attributeName;
  }
}
