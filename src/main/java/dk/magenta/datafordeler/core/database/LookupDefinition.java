package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.exception.PluginImplementationException;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.core.util.DoubleHashMap;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by lars on 13-06-17.
 * A LookupDefinition is a way of defining how to look up entities based on the data hierarchy within them.
 * Since the data in an entity is spread out over multiple tables, it is difficult to do a database select
 * on a field when you don't know where it is. So, DataItem subclasses and Query subclasses should implement
 * a method returning a LookupDefinition. At its most basic, it's a map of path strings to sought values.
 *
 * Examples of key-value pairs:
 *
 * coreData.attribute = 123
 * This means that for a given DataItem table, join its "coreData" table and match on attribute = 123
 *
 * $.foo = 42
 * This means that we should look in the Entity table (instead of the DataItem table) for a match on foo = 42
 *
 * Of course the map can contain any number of these key-value pairs, so they'll all be AND'ed together
 * in the resulting query
 *
 *
 *
 * Full usage example:
 *
 * LookupDefinition lookupDefinition = new LookupDefinition(Collections.singletonMap("foo.bar", 42))
 *
 *
 *
 * Class<D> dClass = FooDataItem.class
 * String dataItemKey = "d";
 * String entityKey = "e";
 * String join = lookupDefinition.getHqlJoinString(dataItemKey, entityKey);
 * String where = lookupDefinition.getHqlWhereString(dataItemKey, entityKey);
 *  org.hibernate.query.Query<D> query = session.createQuery(
 *      "select " + dataItemKey + " from " + dClass.getCanonicalName() + " "+dataItemKey+
 *      " " + join + " where " + where, dClass
 *      );
 * HashMap<String, Object> parameters = lookupDefinition.getHqlParameters(dataItemKey, entityKey);
 * for (String key : parameters.keySet()) {
 *    query.setParameter(key, parameters.get(key));
 * }
 *
 * This would look up items of the FooDataItem class where the subtable foo has a variable bar with value 42.
 * See also the various uses in QueryManager, which perform database lookups based on LookupDefinitions from
 * Query and DataItem objects
 */
public class LookupDefinition extends HashMap<String, Object> {

    private Query query = null;
    private boolean matchNulls = false;
    public static final String separator = ".";
    public static final String entityref = "$";
    private static final String quotedSeparator = Pattern.quote(separator);

    public LookupDefinition() {
    }

    public LookupDefinition(Query query) {
        this.query = query;
    }

    public LookupDefinition(Map<String, Object> map) {
        this.putAll(map);
    }

    public LookupDefinition(Map<String, Object> map, Query query) {
        this(map);
        this.query = query;
    }

    /**
     * Under a given top key, put a map of lookups into our hashmap.
     * For example: putAll("abc", {"def": 23, "ghi": 42}) will result in
     * {"abc.def": 23, "abc.ghi": 42}
     * @param topKey
     * @param map
     */
    public void putAll(String topKey, Map<String, Object> map) {
        for (String key : map.keySet()) {
            this.put(topKey + separator + key, map.get(key));
        }
    }

    public void setMatchNulls(boolean matchNulls) {
        this.matchNulls = matchNulls;
    }

    /**
     * Obtain the table join string, including all tables that have been added to the LookupDefinition
     * @param rootKey Root key, denoting the baseline for the join. This is most often the hql identifier
     *                for the DataItem table: if the HQL so far is "SELECT e from FooEntity JOIN e.registrations r JOIN r.effects v JOIN v.dataItems d",
     *                then "d" would be the rootKey to look up paths within the dataItem table
     * @param entityKey Entity key, denoting the hql identifier for the Entity table. In the above example, "e" would be the entityKey
     * @return join string, e.g. "JOIN d.abc d_abc JOIN e.foo e_foo"
     */
    public String getHqlJoinString(String rootKey, String entityKey) {
        ArrayList<String> joinTables = new ArrayList<>();
        for (String key : this.keySet()) {
            if (key.contains(separator)) {
                String[] parts = key.split(quotedSeparator);
                if (parts[0].equals(entityref)) {
                    parts = Arrays.copyOfRange(parts, 1, parts.length);
                }
                StringBuilder fullParts = new StringBuilder(rootKey);
                for (int i = 0; i<parts.length - 1; i++) {
                    String part = parts[i];
                    fullParts.append("_").append(part);
                    String joinEntry = fullParts + "." + part + " " + fullParts;
                    if (!joinTables.contains(joinEntry)) {
                        joinTables.add(joinEntry);
                    }
                }
            }
        }
        if (!joinTables.isEmpty()) {
            StringJoiner s = new StringJoiner(" JOIN ");
            for (String table : joinTables) {
                s.add(table);
            }
            return "JOIN " + s.toString();
        }
        return "";
    }


    public String getHqlWhereString(String rootKey, String entityKey) {
        return this.getHqlWhereString(rootKey, entityKey, "AND");
    }

    /**
     * Obtain the table where string, specifying the hql WHERE statement for each value in the LookupDefinition
     * Used in conjunction with getHqlJoinString (and using the same input keys).
     *
     * @param rootKey Root key, denoting the baseline for the join. This is most often the hql identifier
     *                for the DataItem table: if the HQL so far is "SELECT e from FooEntity JOIN e.registrations r JOIN r.effects v JOIN v.dataItems d",
     *                then "d" would be the rootKey to look up paths within the dataItem table
     * @param entityKey Entity key, denoting the hql identifier for the Entity table. In the above example, "e" would be the entityKey
     * @param prefix prefix string to prepend the output if it is not empty
     * @return where string, e.g. " AND d_abc.def = :d_abc_def AND d_abc.ghi = :d_abc_ghi
     */
    public String getHqlWhereString(String rootKey, String entityKey, String prefix) {
        StringJoiner s = new StringJoiner(" AND ");
        for (String key : this.keySet()) {

            String object = this.getPath(rootKey, entityKey, key);
            String k = getFinal(key);

            Object value = this.get(key);

            if (value == null) {
                if (this.matchNulls) {
                    s.add(object + "." + k + " is null");
                }
            } else {
                if (value instanceof List) {
                    List list = (List) value;
                    StringJoiner or = new StringJoiner(" OR ");
                    for (int i=0; i<list.size(); i++) {
                        if (parameterValueWildcard(list.get(i))) {
                            or.add("cast(" + object + "." + k + " as string) like :" + object + "_" + k + "_" + i);
                        } else {
                            or.add(object + "." + k + " = :" + object + "_" + k + "_" + i);
                        }
                    }
                    if (or.length() > 0) {
                        s.add("(" + or.toString() + ")");
                    }
                } else {
                    if (parameterValueWildcard(value)) {
                        s.add("cast(" + object + "." + k + " as string) like :" + object + "_" + k);
                    } else {
                        s.add(object + "." + k + " = :" + object + "_" + k);
                    }
                }
            }

        }
        if (s.length() > 0) {
            return prefix + " " + s.toString();
        }
        return "";
    }

    /**
     * Convert keys to a path.
     * @param rootKey table root key
     * @param entityKey entity key
     * @param key dot-spearated path, e.g. foo.bar.baz
     * @return converted path. e.g. (rootKey: "d", entityKey: "e", key: "foo.bar.baz") => "d_foo_bar_baz" or (rootKey: "d", entityKey: "e", key: "$.bar.baz") => "e_bar_baz"
     */
    private String getPath(String rootKey, String entityKey, String key) {
        int separatorIndex = key.indexOf(separator);
        String first = separatorIndex != -1 ? key.substring(0, separatorIndex) : key;
        String object = rootKey;
        if (first.equals(entityref)) {
            object = entityKey;
            key = key.substring(separatorIndex + 1);
        }
        if (key.contains(separator)) {
            object += "_" + key.substring(0, key.lastIndexOf(separator)).replaceAll(quotedSeparator, "_");
        }
        return object;
    }

    /**
     * Get the substring after the last separator. If no separator is found, the whole key
     * @param key
     * @return
     */
    private String getFinal(String key) {
        if (key.contains(separator)) {
            return key.substring(key.lastIndexOf(separator) + 1);
        } else {
            return key;
        }
    }

    /**
     * Obtain the values defined in the LookupDefinition, with their paths normalized to match what would be output by getWhereString()
     * @param rootKey Root key, denoting the baseline for the join. This is most often the hql identifier
     *                for the DataItem table: if the HQL so far is "SELECT e from FooEntity JOIN e.registrations r JOIN r.effects v JOIN v.dataItems d",
     *                then "d" would be the rootKey to look up paths within the dataItem table
     * @param entityKey Entity key, denoting the hql identifier for the Entity table. In the above example, "e" would be the entityKey
     * @return Map to be used for filling the query parameters. E.g. {"d_abc_def": 23, "d_abc_ghi": 42}
     * @throws PluginImplementationException
     */
    public HashMap<String, Object> getHqlParameters(String rootKey, String entityKey) throws PluginImplementationException {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : this.keySet()) {

            String object = this.getPath(rootKey, entityKey, key);
            String k = getFinal(key);

            Object value = this.get(key);
            if (value != null) {
                if (value instanceof List) {
                    List list = (List) value;
                    for (int i=0; i<list.size(); i++) {
                        if (parameterValueWildcard(value)) {
                            map.put(object + "_" + k + "_" + i, ((String) list.get(i)).replace("*", "%"));
                        } else {
                            map.put(object + "_" + k + "_" + i, this.castValue(object + "_" + k + "_" + i, list.get(i)));
                        }
                    }
                } else {
                    if (parameterValueWildcard(value)) {
                        map.put(object + "_" + k, ((String) value).replace("*", "%"));
                    } else {
                        map.put(object + "_" + k, this.castValue(object + "_" + k, value));
                    }
                }
            }

        }
        return map;
    }

    private Object castValue(String path, Object value) throws PluginImplementationException {
        if (this.query != null) {
            Class cls;
            String[] parts = path.split("_");
            if (parts[0].equals(QueryManager.ENTITY)) {
                cls = this.query.getEntityClass();
            } else {
                cls = this.query.getDataClass();
            }

            Field field = null;
            for (int i = 1; i<parts.length; i++) {
                String part = parts[i];
                try {
                    field = this.getField(cls, part);
                } catch (NoSuchFieldException e) {
                    throw new PluginImplementationException("Field '"+part+"' is missing from class "+cls, e);
                }
                cls = field.getType();
            }

            if (field != null && field.isAnnotationPresent(Column.class)) {
                if (cls == Integer.TYPE && !(value instanceof Integer)) {
                    value = Integer.parseInt((String) value);
                } else if (cls == Boolean.TYPE && !(value instanceof Boolean)) {
                    value = Query.booleanFromString((String) value);
                }
            }
        }
        return value;
    }

    private Field getField(Class cls, String name) throws NoSuchFieldException {
        Class c = cls;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static boolean parameterValueWildcard(Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            return stringValue.startsWith("*") || stringValue.endsWith("*");
        }
        return false;
    }

}
