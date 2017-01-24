package dk.magenta.datafordeler.core.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lars on 11-01-17.
 */
public class ConfigurationFieldSpecification<T> {

  private String fieldName;
  private String label;
  private String helpText;
  private List<T> choices;
  private T defaultValue;

  public ConfigurationFieldSpecification(String fieldName, String label, T defaultValue,
      String helpText) {
    this.fieldName = fieldName;
    this.label = label;
    this.defaultValue = defaultValue;
    this.helpText = helpText;
    this.choices = new ArrayList<T>();
  }

  public ConfigurationFieldSpecification(String fieldName, String label, T defaultValue) {
    this(fieldName, label, defaultValue, null);
  }

  public void addChoice(T choice) {
    this.choices.add(choice);
  }

  public void addChoice(int index, T choice) {
    this.choices.add(index, choice);
  }

  public void addChoices(Collection<T> choices) {
    this.choices.addAll(choices);
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getLabel() {
    return label;
  }

  public String getHelpText() {
    return helpText;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public List<T> getChoices() {
    return choices;
  }
}
