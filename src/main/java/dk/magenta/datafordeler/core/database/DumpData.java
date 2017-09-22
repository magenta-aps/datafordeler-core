package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@javax.persistence.Entity
@Table(name = "dump_data")
public class DumpData extends DatabaseEntry {
    @Column(nullable = false)
    @Type(type = "text")
    private String data;

    public DumpData() {

    }

    public DumpData(String data) {
        this.data = data;
    }

    String getData() {
        return data;
    }
}
