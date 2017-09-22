package dk.magenta.datafordeler.core.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@javax.persistence.Entity
@Table(name = "dump")
public final class DumpMetadata extends DatabaseEntry implements
    Comparable<DumpMetadata> {

    @Column
    private String plugin, entityName, format;

    @Column
    private OffsetDateTime timestamp;

    @Column
    @Type(type = "text")
    private String data;

    public DumpMetadata() {
    }

    public DumpMetadata(String plugin, String entityName, String format,
        OffsetDateTime timestamp, String data) {
        this.plugin = plugin;
        this.entityName = entityName;
        this.format = format;
        this.data = data;
        this.timestamp = timestamp;
    }

  @Override
  public int compareTo(DumpMetadata o) {
    return Long.compare(this.getId(), o.getId());
  }

  @JsonIgnore
  public String getPlugin() {
    return this.plugin;
  }

  @JsonIgnore
  public String getEntityName() { return this.entityName; }

  @JsonIgnore
  public String getData() { return this.data; }

  @JsonIgnore
  public String getFormat() { return format; }

  @JsonIgnore
  public OffsetDateTime getTimestamp() { return timestamp; }
}
