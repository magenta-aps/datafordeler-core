package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Abstract superclass for all object classes
 */
@MappedSuperclass
@Embeddable
public abstract class DatabaseEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonIgnore
    private Long id;

    public Long getId() {
        return this.id;
    }

}
