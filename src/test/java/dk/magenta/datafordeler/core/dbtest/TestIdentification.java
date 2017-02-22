package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.Identification;

import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_identification")
public class TestIdentification extends Identification {

    public TestIdentification() {}

    public TestIdentification(UUID uuid, String domain) {
        super(uuid, domain);
    }
}
