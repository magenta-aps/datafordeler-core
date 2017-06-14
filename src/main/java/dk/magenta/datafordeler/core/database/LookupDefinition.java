package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.util.DoubleHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by lars on 13-06-17.
 */
public class LookupDefinition extends DoubleHashMap<String, String, Object> {

    public static final String self = "__self__";

    public LookupDefinition() {
    }

    public LookupDefinition(Map<String, Object> map) {
        for (String key : map.keySet()) {
            this.put(self, key, map.get(key));
        }
    }

    public String getHqlJoinString(String root) {
        return this.getHqlJoinString(root, true);
    }

    public String getHqlJoinString(String root, boolean withPrefix) {
        StringJoiner s = new StringJoiner(" JOIN ");
        for (String table : this.keySet()) {
            if (!table.equals(self)) {
                s.add(root + "." + table + " " + root + "_" + table);
            }
        }
        if (s.length() > 0) {
            return "JOIN " + s.toString();
        }
        return "";
    }

    public String getHqlWhereString(String root) {
        return this.getHqlWhereString(root, "AND");
    }

    public String getHqlWhereString(String root, String prefix) {
        StringJoiner s = new StringJoiner(" AND ");
        for (String table : this.keySet()) {
            String object = table.equals(self) ? root : root+"_"+table;
            for (String key : this.get(table).keySet()) {
                if (this.get(table, key) == null) {
                    s.add(object + "." + key + " is null");
                } else {
                    s.add(object + "." + key + "=:" + object+"_"+key);
                }
            }
        }
        if (s.length() > 0) {
            return prefix + " " + s.toString();
        }
        return "";
    }

    public HashMap<String, Object> getHqlParameters(String root) {
        HashMap<String, Object> map = new HashMap<>();
        for (String table : this.keySet()) {
            String object = table.equals(self) ? root : root+"_"+table;
            for (String key : this.get(table).keySet()) {
                Object value = this.get(table, key);
                if (value != null) {
                    map.put(object + "_" + key, value);
                }
            }
        }
        System.out.println(map);
        return map;
    }

}
