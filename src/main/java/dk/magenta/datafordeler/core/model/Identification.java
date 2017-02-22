package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Identification {

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
}
