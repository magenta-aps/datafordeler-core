package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.database.Registration;

/**
 * Created by lars on 10-05-17.
 */
public class DuplicateSequenceNumberException extends InvalidDataInputException {

    private Registration existingRegistration;
    private Registration newRegistration;

    public DuplicateSequenceNumberException(Registration newRegistration, Registration existingRegistration) {
        super("Duplicate sequencenumber " + newRegistration.getSekvensnummer() + ", shared between existing registrering "+existingRegistration.getRegisterChecksum()+" and new registrering "+newRegistration.getRegisterChecksum());
        this.newRegistration = newRegistration;
        this.existingRegistration = existingRegistration;
    }

    public Registration getExistingRegistration() {
        return this.existingRegistration;
    }

    public Registration getNewRegistration() {
        return this.newRegistration;
    }

    @Override
    public String getCode() {
        return "datafordeler.import.duplicate_sequence_number";
    }
}
