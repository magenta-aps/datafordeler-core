package dk.magenta.datafordeler.core.plugin;

import java.util.Collection;

/**
 * Created by lars on 11-01-17.
 */
public abstract class DataEntity<T extends DataVersion> {
  protected String id;
  // public static BaseEntity<T> FromJSON(JSONObject data);
  protected Collection<T> versions;

  public String getId() {
    return id;
  }
}
