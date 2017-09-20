package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.database.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexander on 12-07-17.
 */
public abstract class OutputWrapper<E extends Entity> {

    public abstract Object wrapResult(E input);

    public final List<Object> wrapResults(Collection<E> input) {
        ArrayList<Object> result = new ArrayList<>();
        for (E item : input) {
            result.add(wrapResult(item));
        }
        return result;
    }
}
