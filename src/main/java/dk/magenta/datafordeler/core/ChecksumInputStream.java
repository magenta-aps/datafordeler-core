package dk.magenta.datafordeler.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by lars on 16-02-17.
 */
public class ChecksumInputStream extends ObjectInputStream {

    public ChecksumInputStream(InputStream in) throws IOException {
        super(in);
    }

    public Checksum next() throws IOException {
        try {
            // Return a Checksum object from the stream; only Checksum objects
            // should be present there
            return (Checksum) this.readObject();
        } catch (ClassNotFoundException e) {
            // The next object in the stream is of a class we can't find
            // This would mean that there's a non-Checksum object in there.
            // Ignore it and get the next one instead
            return this.next();
        }
    }
}
