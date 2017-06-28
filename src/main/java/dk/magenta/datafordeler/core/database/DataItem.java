package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.Session;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 20-02-17.
 */
@MappedSuperclass
@Embeddable
// @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class DataItem<V extends Effect, D extends DataItem> extends DatabaseEntry {

    public DataItem() {
    }

    public void addEffect(V effect) {
        effect.dataItems.add(this);
    }

    public void removeEffect(V effect) {
        effect.dataItems.remove(this);
    }

    public static String getTableName(Class<? extends DataItem> cls) {
        return cls.getAnnotation(Table.class).name();
    }

    @ManyToMany(mappedBy = "dataItems")
    private Set<V> effects = new HashSet<V>();


    /**
     * Compares this object with another DataItem
     * @param other
     * @return
     */
    public boolean equalData(D other) {
        return this.asMap().equals(other.asMap());
    }

    /**
     * Obtain contained data as a Map
     * Used for serializing DataItems merged into one wrapper
     * @return Map of all relevant attributes
     */
    public abstract Map<String, Object> asMap();

    /**
     * Obtain contained data as a Map
     * Internally used for comparing DataItems
     * @return Map of all relevant attributes
     */
    public Map<String, Object> databaseFields() {
        return this.asMap();
    }

    /**
     * Pretty-print contained data
     * @return Compiled string output
     */
    public String toString() {
        return this.toString(0);
    }

    /**
     * Pretty-print contained data
     * @param indent Number of spaces to indent the output with
     * @return Compiled string output
     */
    public String toString(int indent) {
        String indentString = new String(new char[4 * (indent)]).replace("\0", " ");
        String subIndentString = new String(new char[4 * (indent + 1)]).replace("\0", " ");
        StringJoiner s = new StringJoiner("\n");
        s.add(indentString + this.getClass().getSimpleName() + "["+this.hashCode()+"] {");

        Map<String, Object> map = this.asMap();
        for (String key : new TreeSet<>(map.keySet())) {
            s.add(subIndentString + key + ": " + map.get(key));
        }

        s.add(indentString + "}");
        return s.toString();
    }

    public HashMap<String, Identification> getReferences() {
        return new HashMap<>();
    }

    public void updateReferences(HashMap<String, Identification> references) {
    }

    public LookupDefinition getLookupDefinition() {
        return new LookupDefinition(this.databaseFields());
    }

    public void forceLoad(Session session) {
    }

}
