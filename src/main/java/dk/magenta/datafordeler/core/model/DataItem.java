package dk.magenta.datafordeler.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
public abstract class DataItem<V extends Effect, D extends DataItem> {

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    protected Set<V> effects;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
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

    /**
     * This member should not be saved to the database; rather, the plugin should populate it,
     * and the engine should then turn that data into a real Effect reference
     */
    @JsonIgnore
    protected OffsetDateTime effectFrom;

    public OffsetDateTime getEffectFrom() {
        return effectFrom;
    }

    public void setEffectFrom(OffsetDateTime effectFrom) {
        this.effectFrom = effectFrom;
    }

    /**
     * This member should not be saved to the database; rather, the plugin should populate it,
     * and the engine should then turn that data into a real Effect reference
     */
    @JsonIgnore
    protected OffsetDateTime effectTo;

    public OffsetDateTime getEffectTo() {
        return effectTo;
    }

    public void setEffectTo(OffsetDateTime effectTo) {
        this.effectTo = effectTo;
    }

    public abstract boolean equalData(D other);

    public abstract Map<String, Object> asMap();
}
