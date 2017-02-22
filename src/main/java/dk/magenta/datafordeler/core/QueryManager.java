package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.model.Entity;
import dk.magenta.datafordeler.core.model.Identification;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by lars on 22-02-17.
 */
@Component
public class QueryManager {

    public <I extends Identification> I getIdentification(Session session, UUID uuid, String domain, Class<I> iClass) {
        Query<I> query = session.createQuery("select i from " + iClass.getName() + " i where i.id = :id and i.domain = :domain", iClass);
        query.setParameter("id", uuid);
        query.setParameter("domain", domain);
        return query.getSingleResult();
    }

    public <E extends Entity> E getEntity(Session session, UUID uuid, String domain, Class<E> iClass) {
        Query<E> query = session.createQuery("select e from " + iClass.getName() + " e join e.identification i where i.id = :id and i.domain = :domain", iClass);
        query.setParameter("id", uuid);
        query.setParameter("domain", domain);
        return query.getSingleResult();
    }
}
