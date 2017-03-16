package dk.magenta.datafordeler.core.oldevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;


/**
 * Created by lars on 13-01-17.
 */

@JsonRootName(value = "Hændelsesbesked")
public class EventMessage {

  @JsonProperty(value = "beskedversion")
  @JacksonXmlProperty(localName = "beskedversion")
  public String messageVersion;

  @JsonProperty(value = "beskedId")
  @JacksonXmlProperty(localName = "beskedId")
  public String messageId;

  @JsonProperty(value = "Beskedkuvert")
  @JacksonXmlProperty(localName = "Beskedkuvert")
  public MessageEnvelope messageEnvelope;

  @JsonProperty(value = "Beskeddata")
  @JacksonXmlProperty(localName = "Beskeddata")
  public ArrayList<MessageData> messageDataList;

}
