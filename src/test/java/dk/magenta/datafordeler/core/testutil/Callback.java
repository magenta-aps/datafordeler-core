package dk.magenta.datafordeler.core.testutil;

import java.util.Map;

/**
 * Created by lars on 29-03-17.
 */
public interface Callback {
    void run(String key, Map<String, String[]> params);
}
