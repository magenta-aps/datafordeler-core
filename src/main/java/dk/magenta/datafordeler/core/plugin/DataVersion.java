package dk.magenta.datafordeler.core.plugin;

/**
 * Created by lars on 11-01-17.
 */
public abstract class DataVersion<T extends DataEntity> {
  protected T entity;
  public abstract String getChecksum();

  public T getEntity() {
    return entity;
  }
}
