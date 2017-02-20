package dk.magenta.datafordeler.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Identification {

    @Id
    private UUID uuid;

    @Column
    private String domain;

    public Identification(UUID uuid, String domain) {
        this.uuid = uuid;
        this.domain = domain;
    }

}
