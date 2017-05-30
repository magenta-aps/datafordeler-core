package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by lars on 11-01-17.
 */
public interface Communicator {

  public abstract InputStream fetch(URI uri) throws HttpStatusException, DataStreamException;

  public abstract StatusLine send(URI endpoint, String payload) throws IOException;

}
