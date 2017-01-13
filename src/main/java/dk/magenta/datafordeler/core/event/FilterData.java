package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by lars on 13-01-17.
 */
public class FilterData {

    @JsonProperty(value="beskedtype")
    public String messageType;

    @JsonProperty(value="beskedansvarligAktør")
    public String messageResponsible;

    @JsonProperty(value="tilladtModtager")
    public String allowedRecipient;

    @JsonProperty(value="RelateretObjekt")
    public String relatedObject;

    @JsonProperty(value="Objektregistrering")
    public ArrayList<ObjectRegistration> objectRegistrations;

    @JsonProperty(value="tværgåendeProces")
    public String transversalProcess;
}
