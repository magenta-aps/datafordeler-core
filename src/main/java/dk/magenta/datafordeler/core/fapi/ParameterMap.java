package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.util.ListHashMap;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Map of URL parameters, where each key can have several values
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

    public static ParameterMap fromPath(String path) {
        ParameterMap map = new ParameterMap();
        if (path.contains("?")) {
            path = path.substring(path.indexOf("?") + 1);
        }
        for (String kvp : path.split("&")) {
            int eqIndex = kvp.indexOf("=");
            String key, value;
            if (eqIndex != -1) {
                key = kvp.substring(0, eqIndex);
                value = kvp.substring(eqIndex + 1);
            } else {
                key = kvp;
                value = "";
            }
            map.add(key, value);
        }
        return map;
    }

    public String[] getAsArray(String key) {
        List<String> values = this.get(key);
        return values.toArray(new String[values.size()]);
    }
}
