package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lars on 13-01-17.
 */
public class ObjectRegistration {

    @JsonProperty(value="registreringsaktør")
    public String registrator;

    @JsonProperty(value="registreringstid")
    public String registrationTime;

    @JsonProperty(value="status")
    public String status;

    @JsonProperty(value="objektansvarligAktør")
    public String objectResponsible;

    @JsonProperty(value="objektID")
    public String objectId;

    @JsonProperty(value="objekttype")
    public String objectType;

    @JsonProperty(value="objekthandling")
    public String objectAction;

    @JsonProperty(value="opgaveemne")
    public String objectSubject;

    @JsonProperty(value="registreringsID")
    public String registrationId;

    @JsonProperty(value="Stedbestemmelse")
    public String localization;
}
