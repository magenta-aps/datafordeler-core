package dk.magenta.datafordeler.core.plugin;

import java.util.Collection;

/**
 * Created by lars on 11-01-17.
 */
public abstract class BaseEntity<T extends BaseVersion> {

  // public static BaseEntity<T> FromJSON(JSONObject data);
  Collection<T> versions;
}
