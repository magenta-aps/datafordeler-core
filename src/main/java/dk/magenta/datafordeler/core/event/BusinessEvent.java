package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.IOException;

/**
 * Created by lars on 13-01-17.
 */
public class BusinessEvent {

    @JsonProperty(value="@odata.type")
    public String eventType;

    @JsonProperty(value="Format")
    public String format;

    @JsonProperty(value="Id")
    public int id;


    private String body;

    @JsonProperty(value="Body")
    public void setBody(String body) {
        this.body = body;
    }

    @JsonProperty(value="Body")
    public String getBody() {
        if (this.eventBody != null) {
            return "";
        } else {
            return this.body;
        }
    }

    private EventBody eventBody;

    @JsonIgnore
    public EventBody getEventBody() throws IOException {
        if (this.eventBody == null) {
            ObjectMapper mapper = new ObjectMapper();
            this.eventBody = mapper.readValue(this.body, EventBody.class);
        }
        return this.eventBody;
    }

    @JsonIgnore
    public void setEventBody(EventBody eventBody) {
        this.eventBody = eventBody;
    }
}
