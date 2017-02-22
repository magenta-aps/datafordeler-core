package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Effect<I extends Identification, E extends Entity, R extends Registration, V extends Effect, D extends DataItem> {

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    protected R registration;

    @ManyToMany(cascade = CascadeType.ALL)
    protected Set<D> dataItems;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = true, updatable = false)
    OffsetDateTime effectFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    OffsetDateTime effectTo;

    public Effect(R registration) {
        this.registration = registration;
    }

}
