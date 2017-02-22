package dk.magenta.datafordeler.core.plugin;

import java.util.Map;
import java.util.UUID;

/**
 * Created by lars on 11-01-17.
 */
public abstract class Fetcher<T extends Entity> {

  public abstract String fetchEntityData();

  public abstract String fetchEntitiesData();

  public abstract Map<UUID, Map<Integer, String>> getChecksums();

}
