package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Created by lars on 13-01-17.
 */
public class MessageEnvelope {

    @JsonProperty(value="Filtreringsdata")
    @JacksonXmlProperty(localName="Filtreringsdata")
    public FilterData filterData;

    @JsonProperty(value="Leveranceinformation")
    @JacksonXmlProperty(localName="Leveranceinformation")
    public DeliveryInformation deliveryInformation;

    @JsonProperty(value="Modtagerhandling")
    @JacksonXmlProperty(localName="Modtagerhandling")
    public String recipientAction;
}
