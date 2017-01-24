package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Created by lars on 13-01-17.
 */
public class ObjectRegistration {

  @JsonProperty(value = "registreringsaktør")
  @JacksonXmlProperty(localName = "registreringsaktør")
  public String registrator;

  @JsonProperty(value = "registreringstid")
  @JacksonXmlProperty(localName = "registreringstid")
  public String registrationTime;

  @JsonProperty(value = "status")
  @JacksonXmlProperty(localName = "status")
  public String status;

  @JsonProperty(value = "objektansvarligAktør")
  @JacksonXmlProperty(localName = "objektansvarligAktør")
  public String objectResponsible;

  @JsonProperty(value = "objektID")
  @JacksonXmlProperty(localName = "objektID")
  public String objectId;

  @JsonProperty(value = "objekttype")
  @JacksonXmlProperty(localName = "objekttype")
  public String objectType;

  @JsonProperty(value = "objekthandling")
  @JacksonXmlProperty(localName = "objekthandling")
  public String objectAction;

  @JsonProperty(value = "opgaveemne")
  @JacksonXmlProperty(localName = "opgaveemne")
  public String objectSubject;

  @JsonProperty(value = "registreringsID")
  @JacksonXmlProperty(localName = "registreringsID")
  public String registrationId;

  @JsonProperty(value = "Stedbestemmelse")
  @JacksonXmlProperty(localName = "Stedbestemmelse")
  public String localization;

  public ObjectRegistration() {

  }

  public ObjectRegistration(String aktor) {

  }
}
