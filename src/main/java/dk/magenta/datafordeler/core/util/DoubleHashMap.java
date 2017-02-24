package dk.magenta.datafordeler.core.util;

import java.util.HashMap;

/**
 * Created by lars on 23-02-17.
 */
public class DoubleHashMap<K, S, V> extends HashMap<K, HashMap<S, V>> {

    public boolean containsKey(K key, S subKey) {
        if (super.containsKey(key)) {
            if (this.get(key).containsKey(subKey)) {
                return true;
            }
        }
        return false;
    }

    public void put(K key, S subKey, V value) {
        HashMap<S, V> subMap = super.get(key);
        if (subMap == null) {
            subMap = new HashMap<S, V>();
            super.put(key, subMap);
        }
        subMap.put(subKey, value);
    }

    public V get(K key, S subKey) {
        HashMap<S, V> subMap = super.get(key);
        if (subMap != null) {
            return subMap.get(subKey);
        }
        return null;
    }
}
