package dk.magenta.datafordeler.core.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;

@Component
public class Stopwatch {

    private HashMap<String, Long> results = new HashMap<>();
    private HashMap<String, Long> running = new HashMap<>();

    public void start(String key) {
        this.running.put(key, Instant.now().toEpochMilli());
    }

    public void measure(String key) {
        if (running.containsKey(key)) {
            long time = Instant.now().toEpochMilli() - this.running.get(key);
            Long old = results.get(key);
            results.put(key, (old == null ? 0L : old) + time);
        }
    }

    public long getTotal(String key) {
        Long total = this.results.get(key);
        return total != null ? total : 0;
    }

    public void reset(String key) {
        this.results.put(key, 0L);
    }

    public String formatTotal(String key) {
        return key + ": " + this.getTotal(key) + "ms";
    }

}
