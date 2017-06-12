package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.util.ListHashMap;

import java.util.List;
import java.util.Map;

/**
 * Created by lars on 12-06-17.
 */
public class ParameterMap extends ListHashMap<String, String> {
    public ParameterMap() {
    }

    public ParameterMap(Map<String, List<String>> initial) {
        super(initial);
    }
}
