package dk.magenta.datafordeler.core.plugin;

/**
 * Created by lars on 11-01-17.
 */
public abstract class BaseVersion<T> {

  private T entity;

  public abstract String getChecksum();
}
