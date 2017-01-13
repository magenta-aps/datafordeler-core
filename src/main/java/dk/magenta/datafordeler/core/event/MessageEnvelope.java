package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lars on 13-01-17.
 */
public class MessageEnvelope {

    @JsonProperty(value="Filtreringsdata")
    public FilterData filterData;

    @JsonProperty(value="Leveranceinformation")
    public DeliveryInformation deliveryInformation;

    @JsonProperty(value="Modtagerhandling")
    public String recipientAction;
}
