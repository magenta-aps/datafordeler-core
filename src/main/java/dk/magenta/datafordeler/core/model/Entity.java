package dk.magenta.datafordeler.core.model;

import org.hibernate.Session;
import org.hibernate.query.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Embeddable
public abstract class Entity<E extends Entity, R extends Registration, V extends Effect> {

    @OneToOne(cascade = CascadeType.ALL)
    protected Identification identification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    protected Set<R> registrations;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Entity() {
        this.registrations = new HashSet<R>();
    }

    public Entity(Identification identification) {
        this();
        this.identification = identification;
    }

    public Entity(UUID uuid, String domain) {
        this(new Identification(uuid, domain));
    }

    public Identification getIdentification() {
        return this.identification;
    }

    public UUID getUUID() {
        return this.identification.getUuid();
    }

    public String getDomain() {
        return this.identification.getDomain();
    }

    public Set<R> getRegistrations() {
        return this.registrations;
    }

    public static String getTableName(Class<? extends Entity> cls) {
        return cls.getAnnotation(Table.class).name();
    }

}
