package dk.magenta.datafordeler.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public abstract class Entity<E extends Entity, R extends Registration> {

    public static String getSchema() {
        return "Entity";
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    protected Identification identification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    protected Set<R> registrations;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Entity() {
        this.registrations = new HashSet<R>();
        this.identification = new Identification();
    }

    public Entity(Identification identification) {
        this();
        this.identification = identification;
    }

    public Entity(UUID uuid, String domain) {
        this(new Identification(uuid, domain));
    }

    @JsonIgnore
    public Identification getIdentification() {
        return this.identification;
    }

    @JsonProperty("uuid")
    public UUID getUUID() {
        return this.identification.getUuid();
    }

    @JsonProperty("uuid")
    public void setUUID(UUID uuid) {
        this.identification.setUuid(uuid);
    }

    public String getDomain() {
        return this.identification.getDomain();
    }

    @JsonProperty
    public void setDomain(String domain) {
        this.identification.setDomain(domain);
    }

    public Long getId() {
        return this.id;
    }

    public Set<R> getRegistrations() {
        return this.registrations;
    }

    public static String getTableName(Class<? extends Entity> cls) {
        return cls.getAnnotation(Table.class).name();
    }

}
