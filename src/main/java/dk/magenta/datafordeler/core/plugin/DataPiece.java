package dk.magenta.datafordeler.core.plugin;

import java.time.OffsetDateTime;

/**
 * Plugins should subclass this and fill instances with incoming data
 */
public abstract class DataPiece {

    protected OffsetDateTime effectiveFrom;
    protected OffsetDateTime effectiveTo;

    // TODO: also add registrationFrom and registrationTo?

    public DataPiece(){
    }

    public OffsetDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public OffsetDateTime getEffectiveTo() {
        return effectiveTo;
    }
}
