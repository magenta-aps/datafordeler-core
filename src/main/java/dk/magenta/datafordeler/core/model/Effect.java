package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Effect<E extends Entity, R extends Registration, V extends Effect> {

    @ManyToOne
    Registration registration;

    @Column
    OffsetDateTime effectFrom;

    @Column
    OffsetDateTime effectTo;

}
