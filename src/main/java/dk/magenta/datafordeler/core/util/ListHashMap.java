package dk.magenta.datafordeler.core.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lars on 23-02-17.
 */
public class ListHashMap<K, V> extends HashMap<K, ArrayList<V>> {

    public void add(K key, V value) {
        ArrayList<V> list = super.get(key);
        if (list == null) {
            list = new ArrayList<V>();
            super.put(key, list);
        }
        list.add(value);
    }

    public V get(K key, int index) {
        ArrayList<V> list = super.get(key);
        if (list != null) {
            return list.get(index);
        }
        return null;
    }
}
