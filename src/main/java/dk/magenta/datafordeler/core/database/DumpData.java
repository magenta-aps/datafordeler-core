package dk.magenta.datafordeler.core.database;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@javax.persistence.Entity
@Table(name = "dump")
public final class DumpData extends DatabaseEntry implements
    Comparable<DumpData> {

    @Column
    private String plugin, entityName, format;

    @Column
    private OffsetDateTime timestamp;

    @Column
    @Type(type = "text")
    private String data;

    public DumpData() {
    }

    public DumpData(String plugin, String entityName, String format,
        String data, OffsetDateTime timestamp) {
        this.plugin = plugin;
        this.entityName = entityName;
        this.format = format;
        this.data = data;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(DumpData o) {
        return Long.compare(this.getId(), o.getId());
    }
}
