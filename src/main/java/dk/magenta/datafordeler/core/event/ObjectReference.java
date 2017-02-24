package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Created by lars on 13-01-17.
 */
public class ObjectReference {

  @JsonProperty(value = "objektreference")
  @JacksonXmlProperty(localName = "objektreference")
  public String objectReference;

}
