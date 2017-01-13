package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by lars on 13-01-17.
 */
public class DeliveryInformation {

    @JsonProperty(value="dannelsestidspunkt")
    public Date creationTime;

    @JsonProperty(value="transaktionsID")
    public String transactionId;

    @JsonProperty(value="kildesystem")
    public String sourceSystem;

    @JsonProperty(value="kildesystemIPAdresse")
    public String sourceSystemIPAddress;

    @JsonProperty(value="kildesystemAkkreditiver")
    public String sourceSystemCredentials;

    @JsonProperty(value="sikkerhedsklassificering")
    public String securityClassification;

    @JsonProperty(value="Leverancerute")
    public String deliveryRoute;
}
