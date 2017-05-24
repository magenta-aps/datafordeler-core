package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
     * Internally used for comparing and pretty-printing DataItems
     * @return Map of all relevant attributes
     */
    public abstract Map<String, Object> asMap();


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
        for (String key : map.keySet()) {
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

    /*public Set<Identification> getReferences() {
        HashSet<Identification> references = new HashSet<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType() == Identification.class) {
                try {
                    references.add((Identification) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("references size: "+references.size());
        return references;
    }*/
}
