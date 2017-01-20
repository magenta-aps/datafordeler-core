package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Date;

/**
 * Created by lars on 13-01-17.
 */
public class DeliveryInformation {

    @JsonProperty(value="dannelsestidspunkt")
    @JacksonXmlProperty(localName="dannelsestidspunkt")
    public Date creationTime;

    @JsonProperty(value="transaktionsID")
    @JacksonXmlProperty(localName="transaktionsID")
    public String transactionId;

    @JsonProperty(value="kildesystem")
    @JacksonXmlProperty(localName="kildesystem")
    public String sourceSystem;

    @JsonProperty(value="kildesystemIPAdresse")
    @JacksonXmlProperty(localName="kildesystemIPAdresse")
    public String sourceSystemIPAddress;

    @JsonProperty(value="kildesystemAkkreditiver")
    @JacksonXmlProperty(localName="kildesystemAkkreditiver")
    public String sourceSystemCredentials;

    @JsonProperty(value="sikkerhedsklassificering")
    @JacksonXmlProperty(localName="sikkerhedsklassificering")
    public String securityClassification;

    @JsonProperty(value="Leverancerute")
    @JacksonXmlProperty(localName="Leverancerute")
    public String deliveryRoute;
}
