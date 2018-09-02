package dk.magenta.datafordeler.core.database;

import java.time.OffsetDateTime;

public interface Nontemporal<E extends IdentifiedEntity> {

    String FILTER_LASTUPDATED_AFTER = "lastupdatedAfterFilter";
    String FILTER_LASTUPDATED_BEFORE = "lastupdatedBeforeFilter";
    String FILTERPARAM_LASTUPDATED_AFTER = "lastupdatedAfterDate";
    String FILTERPARAM_LASTUPDATED_BEFORE = "lastupdatedBeforeDate";
    String FILTERLOGIC_LASTUPDATED_AFTER = "(" + Nontemporal.DB_FIELD_UPDATED + " >= :" + Monotemporal.FILTERPARAM_LASTUPDATED_AFTER + ")";
    String FILTERLOGIC_LASTUPDATED_BEFORE = "(" + Nontemporal.DB_FIELD_UPDATED + " < :" + Monotemporal.FILTERPARAM_LASTUPDATED_BEFORE + ")";

    String DB_FIELD_ENTITY = "entity";
    E getEntity();
    void setEntity(E entity);

    String DB_FIELD_UPDATED = "dafoUpdated";
    String IO_FIELD_UPDATED = "sidstOpdateret";
    OffsetDateTime getDafoUpdated();
    Nontemporal setDafoUpdated(OffsetDateTime dafoUpdated);
    boolean equalData(Object o);

    static void copy(Nontemporal from, Nontemporal to) {
        to.setDafoUpdated(from.getDafoUpdated());
    }

}
