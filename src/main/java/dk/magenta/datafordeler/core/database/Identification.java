package dk.magenta.datafordeler.core.database;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 * Identifier for Entity objects, easing cross-referencing. A reference need not 
 * locate the referenced Entity in the database (it may not even exist yet), but only
 * locate/generate the relevant Identification based on data that it should know.
 * Each UUID used here should be generated from identifying data, such as Person 
 * Number, Company Number etc, so that a given one of these always results in the 
 * same UUID.
 * That way, we can always locate/create an Identification when we know this seed 
 * data.
 */
@javax.persistence.Entity
@Table(name = "identifikation", indexes = {@Index(name="uuid", columnList = "uuid"), @Index(name="id", columnList = "uuid, domain")})
public final class Identification extends DatabaseEntry implements Comparable<Identification> {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identification that = (Identification) o;

        if (!uuid.equals(that.uuid)) return false;
        return domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + domain.hashCode();
        return result;
    }

    @Override
    public int compareTo(Identification o) {
        if (o == null) return 1;
        return this.uuid.compareTo(o.getUuid());
    }
}
