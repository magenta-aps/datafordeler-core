package dk.magenta.datafordeler.core.database;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@javax.persistence.Entity
@Table(name = "identifikation", indexes = {@Index(name="uuid", columnList = "uuid"), @Index(name="id", columnList = "uuid, domaene")})
public class Identification extends DatabaseEntry {

    @Column(unique = true, nullable = false, insertable = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, insertable = true, updatable = false)
    private String domaene;

    public Identification() {
    }

    public Identification(UUID uuid, String domaene) {
        this.uuid = uuid;
        this.domaene = domaene;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDomaene() {
        return domaene;
    }

    public void setDomaene(String domaene) {
        this.domaene = domaene;
    }

    public static String getTableName() {
        return Identification.class.getAnnotation(Table.class).name();
    }
}
