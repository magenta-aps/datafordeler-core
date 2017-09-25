package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "dump_info")
public final class DumpInfo extends DatabaseEntry implements
    Comparable<DumpInfo> {

    @Column(nullable = false)
    private String plugin, entityName, format;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = true,
        cascade = CascadeType.ALL)
    @Lob
    private DumpData data;

    private DumpInfo() {

    }

    public DumpInfo(String plugin, String entityName, String format,
        OffsetDateTime timestamp, String data) {
        this.plugin = plugin;
        this.entityName = entityName;
        this.format = format;
        this.timestamp = timestamp;
        this.data = data != null ? new DumpData(data) : null;
    }

    @Override
    public int compareTo(DumpInfo o) {
        return Long.compare(this.getId(), o.getId());
    }

    @JsonIgnore
    public String getPlugin() {
        return this.plugin;
    }

    @JsonIgnore
    public String getEntityName() {
        return this.entityName;
    }

    @JsonIgnore
    public String getData() {
        try {
            return this.data.getData();
        } catch (NullPointerException e) {
            return null;
        }
    }

    @JsonIgnore
    public String getFormat() {
        return format;
    }

    @JsonIgnore
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}

