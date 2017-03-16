package dk.magenta.datafordeler.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class Registration<E extends Entity, R extends Registration, V extends Effect> {

    @ManyToOne
    @JsonIgnore
    protected E entity;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonProperty
    protected Set<V> effects;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @Column(nullable = false, insertable = true, updatable = false)
    @JsonProperty
    protected OffsetDateTime registrationFrom;

    @Column(nullable = true, insertable = true, updatable = false)
    @JsonProperty
    protected OffsetDateTime registrationTo;

    @JsonProperty
    protected int sequenceNumber;

    // The checksum as reported by the register
    @JsonProperty("checksum")
    protected String registerChecksum;

    public Registration() {
        this.effects = new HashSet<V>();
    }

    public Registration(E entity, OffsetDateTime registrationFrom, OffsetDateTime registrationTo) {
        this();
        this.entity = entity;
        this.registrationFrom = registrationFrom;
        this.registrationTo = registrationTo;
        this.entity.registrations.add(this);
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

    public E getEntity() {
        return this.entity;
    }

    public OffsetDateTime getRegistrationFrom() {
        return this.registrationFrom;
    }

    public OffsetDateTime getRegistrationTo() {
        return this.registrationTo;
    }

    public Set<V> getEffects() {
        return this.effects;
    }

    public V getEffect(OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        for (V effect : this.effects) {
            if (effect.getEffectFrom().equals(effectFrom) && effect.getEffectTo().equals(effectTo)) {
                return effect;
            }
        }
        return null;
    }

    public void removeEffect(V effect) {
        // Be sure to also delete the effect yourself, since it still points to the Registration
        this.effects.remove(effect);
    }

    public static String getTableName(Class<? extends Registration> cls) {
        return cls.getAnnotation(Table.class).name();
    }
}
