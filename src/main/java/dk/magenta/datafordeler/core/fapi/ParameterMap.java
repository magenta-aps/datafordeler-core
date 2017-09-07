package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.util.ListHashMap;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by lars on 12-06-17.
 */
public class ParameterMap extends ListHashMap<String, String> {
    public ParameterMap() {
    }

    public ParameterMap(Map<String, List<String>> initial) {
        super(initial);
    }

    public String asUrlParams() {
        StringJoiner sj = new StringJoiner("&");
        for (String key : this.keySet()) {
            for (String value : this.get(key)) {
                sj.add(key + "=" + value);
            }
        }
        return sj.toString();
    }
}
