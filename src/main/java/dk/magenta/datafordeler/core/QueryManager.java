package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.model.Effect;
import dk.magenta.datafordeler.core.model.Entity;
import dk.magenta.datafordeler.core.model.Identification;
import dk.magenta.datafordeler.core.model.Registration;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Created by lars on 22-02-17.
 */
@Component
public class QueryManager {

    public Identification getIdentification(Session session, UUID uuid) {
        Query<Identification> query = session.createQuery("select i from Identification i where i.id = :id", Identification.class);
        query.setParameter("id", uuid);
        return query.getSingleResult();
    }

    public <E extends Entity> E getEntity(Session session, UUID uuid, Class<E> eClass) {
        Query<E> query = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.id = :id", eClass);
        query.setParameter("id", uuid);
        return query.getSingleResult();
    }

//    public <V extends Effect> V getEffect(Session session, Registration registration, OffsetDateTime effectFrom, OffsetDateTime effectTo) {
//
//    }

}
