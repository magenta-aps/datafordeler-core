package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lars on 11-01-17.
 */
public interface Fetcher {

  // public abstract String fetchEntityData();

  public abstract InputStream fetch(URI uri) throws HttpStatusException, DataStreamException;

  // public abstract String fetchEntitiesData();

  // public abstract Map<UUID, Map<Integer, String>> getChecksums();

}
