package dk.magenta.datafordeler.core.database;

import java.io.Serializable;
import java.net.URI;

/**
 * Created by lars on 13-03-17.
 *
 * A reference is a serializable object passed though the register channel,
 * able to look up a Registration
 */
public interface RegistrationReference extends Serializable {
    String getChecksum();
    URI getURI();
}
