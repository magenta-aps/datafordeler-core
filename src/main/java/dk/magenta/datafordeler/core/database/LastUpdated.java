package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "last_updated")
public class LastUpdated extends DatabaseEntry {

    @Column
    private String plugin;

    @Column
    private String schema;

    @Column
    private OffsetDateTime timestamp;


    public String getPlugin() {
        return this.plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
