package dk.magenta.datafordeler.core.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class DataItem<E extends Entity, R extends Registration, V extends Effect> {

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    protected Set<V> effects;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public DataItem() {
        this.effects = new HashSet<V>();
    }

    public void addEffect(V effect) {
        this.effects.add(effect);
        effect.dataItems.add(this);
    }

    public void removeEffect(V effect) {
        this.effects.remove(effect);
        effect.dataItems.remove(this);
    }

    public static String getTableName(Class<? extends DataItem> cls) {
        return cls.getAnnotation(Table.class).name();
    }

}
