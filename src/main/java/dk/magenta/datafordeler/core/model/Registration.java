package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Set;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Registration<I extends Identification, E extends Entity, R extends Registration, V extends Effect> {

    @ManyToOne
    protected E entity;

    @OneToMany(cascade = CascadeType.ALL)
    protected Set<V> effects;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = true, updatable = false)
    OffsetDateTime registrationFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    OffsetDateTime registrationTo;

    public Registration(E entity, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this.entity = entity;
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
    }

    public Registration(E entity, TemporalAccessor registrationFrom, TemporalAccessor registrationTo) {
        this(
                entity,
                registrationFrom != null ? OffsetDateTime.from(registrationFrom) : null,
                registrationTo != null ? OffsetDateTime.from(registrationTo) : null
        );
    }

    /**
     * @param entity
     * @param registrationFrom A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * @param registrationTo A date string, parseable by DateTimeFormatter.ISO_OFFSET_DATE_TIME (in the format 2007-12-03T10:15:30+01:00)
     * If you want other date formats, consider using java.time.OffsetDateTime.parse() to generate an OffsetDateTime object and pass it
     */
    public Registration(E entity, String registrationFrom, String registrationTo) {
        this(
                entity,
                registrationFrom != null ? OffsetDateTime.parse(registrationFrom) : null,
                registrationTo != null ? OffsetDateTime.parse(registrationTo) : null
        );
    }
}
