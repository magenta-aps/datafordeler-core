package dk.magenta.datafordeler.core.role;

/**
 * Created by jubk on 16-03-2017.
 */
public abstract class SystemRoleVersion {
  private float version;
  private String comment;

  public SystemRoleVersion(float version, String comment) {
    this.version = version;
    this.comment = comment;
  }

  public float getVersion() {
    return version;
  }

  public String getComment() {
    return comment;
  }
}
