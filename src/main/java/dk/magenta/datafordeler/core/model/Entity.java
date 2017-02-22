package dk.magenta.datafordeler.core.model;

import org.hibernate.Session;
import org.hibernate.query.*;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Embeddable
public abstract class Entity<I extends Identification, E extends Entity, R extends Registration, V extends Effect> {

    @OneToOne(cascade = CascadeType.ALL)
    protected I identification;

    @OneToMany(cascade = CascadeType.ALL)
    protected Set<R> registrations;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Entity() {
    }

    public Entity(I identification) {
        this.identification = identification;
    }

    public I getIdentification() {
        return this.identification;
    }

    public UUID getUUID() {
        return this.identification.getUuid();
    }

    public String getDomain() {
        return this.identification.getDomain();
    }

    public static String getTableName(Class<? extends Entity> cls) {
        return cls.getAnnotation(Table.class).name();
    }

}
