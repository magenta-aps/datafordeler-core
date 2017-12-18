package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.RegistrationReference;

import java.net.URI;

public class DemoRegistrationReference implements RegistrationReference {

    private URI uri;

    @Override
    public URI getURI() {
        return this.uri;
    }

    @JsonProperty("checksum")
    public String checksum;

    @Override
    public String getChecksum() {
        return this.checksum;
    }

    @JsonProperty("sekvensnummer")
    public int sequenceNumber;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public DemoRegistrationReference() {
    }

    public DemoRegistrationReference(URI uri) {
        this.uri = uri;
    }

    public DemoRegistrationReference(String checksum, int sequenceNumber) {
        this.checksum = checksum;
        this.sequenceNumber = sequenceNumber;
    }

}
