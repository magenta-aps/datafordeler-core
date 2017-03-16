package dk.magenta.datafordeler.core.oldevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;

/**
 * Created by lars on 13-01-17.
 */
public class FilterData {

  @JsonProperty(value = "beskedtype")
  @JacksonXmlProperty(localName = "beskedtype")
  public String messageType;

  @JsonProperty(value = "beskedansvarligAktør")
  @JacksonXmlProperty(localName = "beskedansvarligAktør")
  public String messageResponsible;

  @JsonProperty(value = "tilladtModtager")
  @JacksonXmlProperty(localName = "tilladtModtager")
  public String allowedRecipient;

  @JsonProperty(value = "RelateretObjekt")
  @JacksonXmlProperty(localName = "RelateretObjekt")
  public String relatedObject;

  @JsonProperty(value = "Objektregistrering")
  @JacksonXmlProperty(localName = "Objektregistrering")
  public ArrayList<ObjectRegistration> objectRegistrations;

  @JsonProperty(value = "tværgåendeProces")
  @JacksonXmlProperty(localName = "tværgåendeProces")
  public String transversalProcess;
}
