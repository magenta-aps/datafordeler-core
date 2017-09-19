package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@javax.persistence.Entity
@Table(name = "dump")
public final class DumpData extends DatabaseEntry implements
    Comparable<DumpData> {
  @Column
  private String plugin;

  @Column
  private String entityName;

  @Column
  @Type(type="text")
  private String data;


  public DumpData() {
  }

  public DumpData(String plugin, String entityName, String data) {
    this.plugin = plugin;
    this.entityName = entityName;
    this.data = data;
  }

  @Override
  public int compareTo(DumpData o) {
    return Long.compare(this.getId(), o.getId());
  }
}
