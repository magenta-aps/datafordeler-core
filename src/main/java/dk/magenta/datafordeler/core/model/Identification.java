package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@javax.persistence.Entity
@Table(name = "identifikation")
public class Identification {

    @Id
    @Column(name = "id")
    private UUID uuid;

    @Column(nullable = false, insertable = true, updatable = true)
    private String domain;

    public Identification() {}

    public Identification(UUID uuid, String domain) {
        this.uuid = uuid;
        this.domain = domain;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDomain() {
        return domain;
    }

    public static String getTableName() {
        return Identification.class.getAnnotation(Table.class).name();
    }
}
