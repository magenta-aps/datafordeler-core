package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.event.BusinessEvent;
import dk.magenta.datafordeler.core.event.DataEvent;
import java.util.List;

/**
 * Created by lars on 11-01-17.
 */

public abstract class Plugin {

  protected long version = 1L;

  protected List<Class> entityClasses;

  protected RegisterHandler registerHandler;

  protected RolesDefinition roleDefinition;

  protected FieldsDefinition fieldsDefinition;

  public Plugin() {
  }

  public long getVersion() {
    return version;
  }

  public void processBusinessEvent(BusinessEvent event) {
    this.registerHandler.processBusinessEvent(event);
  }

  public void processDataEvent(DataEvent event) {
    this.registerHandler.processDataEvent(event);
  }
}
