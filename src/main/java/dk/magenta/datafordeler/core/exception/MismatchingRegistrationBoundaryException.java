package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

import java.time.format.DateTimeFormatter;

/**
 * Created by lars on 10-05-17.
 */
public class MismatchingRegistrationBoundaryException extends InvalidDataInputException {

    private Registration newRegistration;
    private Registration existingRegistration;

    public MismatchingRegistrationBoundaryException(Registration newRegistration, Registration existingRegistration) {
        super("Mismatching timestamps; incoming registration "+newRegistration.getRegisterChecksum()+
                " with registrationFrom "+newRegistration.getRegistrationFrom().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)+
                " does not match existing registration "+existingRegistration.getRegisterChecksum()+" with registerTo "+
                existingRegistration.getRegistrationTo().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        this.newRegistration = newRegistration;
        this.existingRegistration = existingRegistration;
    }

    public Registration getNewRegistration() {
        return newRegistration;
    }

    public Registration getExistingRegistration() {
        return existingRegistration;
    }

    @Override
    public String getCode() {
        return "datafordeler.input.registration_boundary_mismatch";
    }
}
