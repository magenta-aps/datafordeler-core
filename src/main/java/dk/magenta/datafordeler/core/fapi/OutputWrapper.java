package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OutputWrapper<E extends IdentifiedEntity> {

    public enum Mode {
        RVD,
        RDV,
        DRV
    }

    public abstract Object wrapResult(E input, Query query, Mode mode);
    
    public final List<Object> wrapResults(Collection<E> input, Query query, Mode mode) {
            ArrayList<Object> result = new ArrayList<>();
        for (E item : input) {
            if (item != null) {
                result.add(wrapResult(item, query, mode));
            }
        }
        return result;
    }

    public static class NodeWrapper {
        private ObjectNode node;

        public NodeWrapper(ObjectNode node) {
            this.node = node;
        }

        public ObjectNode getNode() {
            return this.node;
        }

        public void put(String key, Boolean value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }
        public void put(String key, Short value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }
        public void put(String key, Integer value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }
        public void put(String key, Long value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }
        public void put(String key, String value) {
            if (value != null) {
                this.node.put(key, value);
            }
        }
        public void set(String key, JsonNode value) {
            if (value != null) {
                this.node.set(key, value);
            }
        }
        public void putPOJO(String key, Object value) {
            if (value != null) {
                this.node.putPOJO(key, value);
            }
        }
    }
}
