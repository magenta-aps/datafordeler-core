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

    public void putAll(String topKey, Map<String, Object> map) {
        for (String key : map.keySet()) {
            this.put(topKey + separator + key, map.get(key));
        }
    }

    public void setMatchNulls(boolean matchNulls) {
        this.matchNulls = matchNulls;
    }

    public String getHqlJoinString(String root) {
        return this.getHqlJoinString(root, true);
    }

    public String getHqlJoinString(String root, boolean withPrefix) {
        ArrayList<String> joinTables = new ArrayList<>();
        for (String key : this.keySet()) {
            if (key.contains(separator)) {
                String[] parts = key.split(quotedSeparator);
                if (parts[0].equals(entityref)) {
                    root = QueryManager.ENTITY;
                    parts = Arrays.copyOfRange(parts, 1, parts.length);
                }
                String lastPart = root;
                StringBuilder fullParts = new StringBuilder(root);
                for (int i = 0; i<parts.length - 1; i++) {
                    String part = parts[i];
                    String joinEntry = lastPart + "." + part + " " + fullParts + "_" + part;
                    if (!joinTables.contains(joinEntry)) {
                        joinTables.add(joinEntry);
                    }

                    //s.add(lastPart + "." + part + " " + fullParts + "_" + part);
                    lastPart = root + "_" + part;
                    fullParts.append("_").append(part);
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


    public String getHqlWhereString(String root) {
        return this.getHqlWhereString(root, "AND");
    }

    public String getHqlWhereString(String root, String prefix) {
        StringJoiner s = new StringJoiner(" AND ");
        for (String key : this.keySet()) {

            String object = this.getPath(root, key);
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

    private String getPath(String root, String key) {
        int separatorIndex = key.indexOf(separator);
        String first = separatorIndex != -1 ? key.substring(0, separatorIndex) : key;
        String object = root;
        if (first.equals(entityref)) {
            object = QueryManager.ENTITY;
            key = key.substring(separatorIndex + 1);
        }
        if (key.contains(separator)) {
            object += "_" + key.substring(0, key.lastIndexOf(separator)).replaceAll(quotedSeparator, "_");
        }
        return object;
    }

    private String getFinal(String key) {
        if (key.contains(separator)) {
            return key.substring(key.lastIndexOf(separator) + 1);
        } else {
            return key;
        }
    }

    public HashMap<String, Object> getHqlParameters(String root) throws PluginImplementationException {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : this.keySet()) {

            String object = this.getPath(root, key);
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
