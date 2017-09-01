package dk.magenta.datafordeler.core.command;

import java.util.Map;
import java.util.StringJoiner;

public abstract class CommandData {

    public abstract boolean containsAll(Map<String, Object> data);
    protected abstract Map<String, Object> contents();

    @Override
    public String toString() {
        StringJoiner contentJoiner = new StringJoiner(", ");
        Map<String, Object> contents = this.contents();
        for (String key : contents.keySet()) {
            contentJoiner.add(key + ": \"" + contents.get(key).toString() + "\"");
        }
        return this.getClass().getSimpleName() + " [" + contentJoiner.toString() + "]";
    }
}
