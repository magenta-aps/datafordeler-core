package dk.magenta.datafordeler.core.oldevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Created by lars on 13-01-17.
 */
public class MessageData {

  @JsonProperty(value = "Objektreference")
  @JacksonXmlProperty(localName = "Objektreference")
  public ObjectReference objectReference;

  @JsonProperty(value = "Objektdata")
  @JacksonXmlProperty(localName = "Objektdata")
  public String objectData;
}
