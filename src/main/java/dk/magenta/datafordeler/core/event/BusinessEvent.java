package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;

/**
 * Created by lars on 13-01-17.
 */
public class BusinessEvent {

  @JsonProperty(value = "@odata.type")
  public String eventType;

  @JsonProperty(value = "Format")
  public String format;

  @JsonProperty(value = "Id")
  public int id;


  private String body;
  private EventBody eventBody;

  @JsonProperty(value = "Body")
  public String getBody() {
    if (this.eventBody != null) {
      return "";
    } else {
      return this.body;
    }
  }

  @JsonProperty(value = "Body")
  public void setBody(String body) {
    this.body = body;
  }

  @JsonIgnore
  public EventBody getEventBody() throws IOException {
    if (this.eventBody == null) {
      if ("JSON".equalsIgnoreCase(this.format)) {
        ObjectMapper mapper = new ObjectMapper();
        this.eventBody = mapper.readValue(this.body, EventBody.class);
      } else if ("XML".equalsIgnoreCase(this.format)) {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper mapper = new XmlMapper(module);
        this.eventBody = new EventBody();
        this.eventBody.eventMessage = mapper.readValue(this.body, EventMessage.class);
      }
    }
    return this.eventBody;
  }

  @JsonIgnore
  public void setEventBody(EventBody eventBody) {
    this.eventBody = eventBody;
  }
}
