package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;


/**
 * Created by lars on 13-01-17.
 */
public class EventMessage {

    @JsonProperty(value="beskedversion")
    public String messageVersion;

    @JsonProperty(value="beskedId")
    public String messageId;

    @JsonProperty(value="Beskedkuvert")
    public MessageEnvelope messageEnvelope;

    @JsonProperty(value="Beskeddata")
    public ArrayList<MessageData> messageDataList;

}
