package dk.magenta.datafordeler.core.model;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Registration<E extends Entity, R extends Registration, V extends Effect> {

    @ManyToOne
    private Entity entity;

    @Column
    OffsetDateTime registrationFrom;

    @Column
    OffsetDateTime registrationTo;
}
