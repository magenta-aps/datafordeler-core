package dk.magenta.datafordeler.core.role;

/**
 * Created by jubk on 16-03-2017.
 */
public class ReadServiceRole extends SystemRole {

  private String serviceName;

  // TODO: serviceName should be replaced with whatever object actually stores the name in the
  // plugin.
  public ReadServiceRole(String serviceName, ReadServiceRoleVersion... versions) {
    super(SystemRoleType.ServiceRole, SystemRoleGrant.Read, null, versions);
    this.serviceName = serviceName;
  }

  @Override
  public String getTargetName() {
    return serviceName;
  }
}
