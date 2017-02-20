package dk.magenta.datafordeler.core.model;

import javax.persistence.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class DataItem<E extends Entity, R extends Registration, V extends Effect> {

    @ManyToMany
    Effect effect;

}
