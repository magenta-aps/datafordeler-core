package dk.magenta.datafordeler.core.model;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Entity<E extends Entity, R extends Registration, V extends Effect> {

    @OneToOne
    private Identification id;



}
