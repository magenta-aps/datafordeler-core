package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.util.Bitemporality;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public interface Bitemporal<E extends IdentifiedEntity> extends Monotemporal<E> {

//    String FILTERPARAM_EFFECT_AFTER = "effectFromDate";
//    String FILTERPARAM_EFFECT_BEFORE = "effectToDate";
//    String FILTER_EFFECT_AFTER = "(" + Bitemporal.DB_FIELD_EFFECT_FROM + " >= :" + Bitemporal.FILTERPARAM_EFFECT_AFTER + " OR " + Bitemporal.DB_FIELD_EFFECT_FROM + " is null)";
//    String FILTER_EFFECT_BEFORE = "(" + Bitemporal.DB_FIELD_EFFECT_FROM + " < :" + Bitemporal.FILTERPARAM_EFFECT_BEFORE + " OR " + Bitemporal.DB_FIELD_EFFECT_FROM + " is null)";

    String FILTER_EFFECT_AFTER = "effectAfterFilter";
    String FILTER_EFFECT_BEFORE = "effectBeforeFilter";
    String FILTERPARAM_EFFECT_AFTER = "effectAfterDate";
    String FILTERPARAM_EFFECT_BEFORE = "effectBeforeDate";

    // True if the sought effect ends after our query
    String FILTERLOGIC_EFFECT_AFTER = "(" + Bitemporal.DB_FIELD_EFFECT_TO + " >= :" + Bitemporal.FILTERPARAM_EFFECT_AFTER + " OR " + Bitemporal.DB_FIELD_EFFECT_TO + " is null)";

    // True if the sought effect begins before our query
    String FILTERLOGIC_EFFECT_BEFORE = "(" + Bitemporal.DB_FIELD_EFFECT_FROM + " < :" + Bitemporal.FILTERPARAM_EFFECT_BEFORE + " OR " + Bitemporal.DB_FIELD_EFFECT_FROM + " is null)";


    String DB_FIELD_EFFECT_FROM = "effectFrom";
    String IO_FIELD_EFFECT_FROM = "virkningFra";
    OffsetDateTime getEffectFrom();
    void setEffectFrom(OffsetDateTime effectFrom);

    String DB_FIELD_EFFECT_TO = "effectTo";
    String IO_FIELD_EFFECT_TO = "virkningTil";
    OffsetDateTime getEffectTo();
    void setEffectTo(OffsetDateTime effectTo);
    
    default Bitemporal setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
        this.setRegistrationFrom(registrationFrom);
        this.setRegistrationTo(registrationTo);
        this.setEffectFrom(effectFrom);
        this.setEffectTo(effectTo);
        return this;
    }

    default Bitemporal setBitemporality(OffsetDateTime registrationFrom, OffsetDateTime registrationTo, TemporalAccessor effectFrom, TemporalAccessor effectTo) {
        this.setRegistrationFrom(registrationFrom);
        this.setRegistrationTo(registrationTo);
        this.setEffectFrom(OffsetDateTime.from(effectFrom));
        this.setEffectTo(OffsetDateTime.from(effectTo));
        return this;
    }

    default Bitemporal setBitemporality(Bitemporality bitemporality) {
        this.setBitemporality(bitemporality.registrationFrom, bitemporality.registrationTo, bitemporality.effectFrom, bitemporality.effectTo);
        return this;
    }
    
    Bitemporality getBitemporality();
    
    static void copy(Bitemporal from, Bitemporal to) {
        Monotemporal.copy(from, to);
        to.setEffectFrom(from.getEffectFrom());
        to.setEffectTo(from.getEffectTo());
    }
}
