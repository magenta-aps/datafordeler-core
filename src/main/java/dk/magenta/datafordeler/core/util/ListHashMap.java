package dk.magenta.datafordeler.core.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A collection mapping keys to lists.
 * Extends HashMap, so the separate lists under each key can be accessed by calling methods on the superclass
 * Created by lars on 23-02-17.
 */
public class ListHashMap<K, V> extends HashMap<K, ArrayList<V>> {

    /**
     * Inserts a value in the list identified by the specified key. Creates a list if none exists.
     * @param key Key to insert by
     * @param value Value to insert
     */
    public void add(K key, V value) {
        ArrayList<V> list = this.get(key);
        if (list == null) {
            list = new ArrayList<>();
            this.put(key, list);
        }
        list.add(value);
    }

    /**
     * Obtains an item from the collection, by key and index
     * @param key Key to look up by
     * @param index Index to look up by
     * @return
     */
    public V get(K key, int index) {
        ArrayList<V> list = super.get(key);
        if (list != null && index < list.size()) {
            return list.get(index);
        }
        return null;
    }
}
