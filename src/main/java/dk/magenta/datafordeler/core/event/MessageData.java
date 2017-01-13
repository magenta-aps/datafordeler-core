package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lars on 13-01-17.
 */
public class MessageData {

    @JsonProperty(value="Objektreference")
    public ObjectReference objectReference;

    @JsonProperty(value="Objektdata")
    public String objectData;
}
