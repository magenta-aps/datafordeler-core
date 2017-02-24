package dk.magenta.datafordeler.core.plugin;

import java.util.Collection;

/**
 * Created by lars on 11-01-17.
 */
public abstract class DataEntity {
  protected String id;
  // public static DataEntity FromJSON(JSONObject data);
  protected Collection<DataVersion> versions;

  public String getId() {
    return id;
  }
}
