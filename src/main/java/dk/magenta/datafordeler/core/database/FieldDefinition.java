package dk.magenta.datafordeler.core.database;

import java.util.*;

public class FieldDefinition {

    public String path;
    public Object value;
    public Class type;
    public LookupDefinition.Operator operator = LookupDefinition.Operator.EQ;
    public int id;
    public HashSet<FieldDefinition> anded = new HashSet<>();
    public HashSet<FieldDefinition> ored = new HashSet<>();
    public boolean inverted = false;
    private static int nextId = 0;

    public FieldDefinition(String path, Object value) {
        this(path, value, value != null ? value.getClass() : null, LookupDefinition.Operator.EQ);
    }
    public FieldDefinition(String path, Object o, Class fieldClass, List<UUID> uuid, Object value) {
        this(path, value, value != null ? value.getClass() : null, LookupDefinition.Operator.EQ);
    }

    public FieldDefinition(String path, Object value, Class type) {
        this(path, value, type, LookupDefinition.Operator.EQ);
    }

   /* public FieldDefinition(String path, Object value, List<UUID> uuid, Class type) {
        this(path, value, type, uuid, LookupDefinition.Operator.EQ);
    }*/

    public FieldDefinition(String path, Object value, Class type, LookupDefinition.Operator operator) {
        this.path = path;
        this.value = value;
        this.type = type;
        this.operator = operator;
        this.id = nextId++;
    }


    public boolean onEntity() {
        return this.path.startsWith(LookupDefinition.entityref);
    }

    public String getOperatorSign() {
        return this.operator.toString();
    }

    public void and(FieldDefinition other) {
        this.anded.add(other);
    }
    public void or(FieldDefinition other) {
        this.ored.add(other);
    }

    public void invert() {
        this.inverted = true;
    }

    public FieldDefinition and(String path, Object value, Class fieldClass) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, LookupDefinition.Operator.EQ);
        this.and(other);
        return other;
    }

    public FieldDefinition and(String path, Object value, Class fieldClass, LookupDefinition.Operator operator) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, operator);
        this.and(other);
        return other;
    }

    public FieldDefinition or(String path, Object value, Class fieldClass) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, LookupDefinition.Operator.EQ);
        this.or(other);
        return other;
    }

    public FieldDefinition or(String path, Object value, Class fieldClass, LookupDefinition.Operator operator) {
        FieldDefinition other = new FieldDefinition(path, value, fieldClass, operator);
        this.or(other);
        return other;
    }

    public Map<String, Object> getParameterMap(String rootKey, String entityKey) {
        HashMap<String, Object> map = new HashMap<>();
        String path = this.path;
        String parameterPath = LookupDefinition.getParameterPath(rootKey, entityKey, path) + "_" + this.id;
        Object value = this.value;
        Class type = this.type;
        if (value != null) {
            if (value instanceof List) {
                List list = (List) value;
                HashSet<Object> nonWildcardItems = new HashSet<>();
                for (int i=0; i<list.size(); i++) {
                    Object item = list.get(i);
                    if (LookupDefinition.parameterValueWildcard(item)) {
                        map.put(parameterPath + "_" + i, LookupDefinition.replaceWildcard(item));
                    } else if (!this.getOperatorSign().equals("=")) {
                        map.put(parameterPath + "_" + i, LookupDefinition.castValue(type, item));
                    } else {
                        nonWildcardItems.add(LookupDefinition.castValue(type, item));
                    }
                }
                if (!nonWildcardItems.isEmpty()) {
                    map.put(parameterPath + "_" + "list", nonWildcardItems);
                }
            } else {
                if (LookupDefinition.parameterValueWildcard(value)) {
                    map.put(parameterPath, LookupDefinition.replaceWildcard(value));
                } else {
                    map.put(parameterPath, LookupDefinition.castValue(type, value));
                }
            }
        }
        for (FieldDefinition other : this.anded) {
            map.putAll(other.getParameterMap(rootKey, entityKey));
        }
        for (FieldDefinition other : this.ored) {
            map.putAll(other.getParameterMap(rootKey, entityKey));
        }
        return map;
    }
}
