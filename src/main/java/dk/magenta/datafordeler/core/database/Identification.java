package dk.magenta.datafordeler.core.database;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@javax.persistence.Entity
@Table(name = "identifikation", indexes = {@Index(name="uuid", columnList = "uuid"), @Index(name="id", columnList = "uuid, domain")})
public class Identification extends DatabaseEntry {

    @Column(unique = true, nullable = false, insertable = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, insertable = true, updatable = false)
    private String domain;

    public Identification() {
    }

    public Identification(UUID uuid, String domain) {
        this.uuid = uuid;
        this.domain = domain;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public static String getTableName() {
        return Identification.class.getAnnotation(Table.class).name();
    }
}
