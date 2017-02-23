package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.model.DataItem;
import dk.magenta.datafordeler.core.model.Effect;
import dk.magenta.datafordeler.core.model.Entity;
import dk.magenta.datafordeler.core.model.Identification;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
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

    public <V extends Effect> List<V> getEffects(Session session, Entity entity, OffsetDateTime effectFrom, OffsetDateTime effectTo, Class<V> vClass) {
        Query<V> query = session.createQuery("select v from " + entity.getClass().getName() + " e join e.registrations r join r.effects v where e.id = :id and v.effectFrom = :from and v.effectTo = :to", vClass);
        query.setParameter("id", entity.getId());
        query.setParameter("from", effectFrom);
        query.setParameter("to", effectTo);
        return query.list();
    }

    /*public <D extends DataItem> List<D> getDataItems(Session session, Entity entity, OffsetDateTime effectFrom, OffsetDateTime effectTo, Class<D> dClass) {
        Query<D> query = session.createQuery("select d from " + entity.getClass().getName() + " e join e.registrations r join r.effects v join v.dataItems d where e.id = :id and v.effectFrom = :from and v.effectTo = :to", dClass);
        query.setParameter("id", entity.getId());
        query.setParameter("from", effectFrom);
        query.setParameter("to", effectTo);
        return query.list();
    }*/

    public <D extends DataItem> List<D> getDataItems(Session session, Entity entity, D similar, Class<D> dClass) {
        StringJoiner s = new StringJoiner(" and ");
        Map<String, Object> similarMap = similar.asMap();
        for (String key : similarMap.keySet()) {
            s.add("d."+key+"=:"+key);
        }
        String entityIdKey = "E" + UUID.randomUUID().toString().replace("-", "");
        Query<D> query = session.createQuery("select d from " + entity.getClass().getName() + " e join e.registrations r join r.effects v join v.dataItems d where e.id = :"+entityIdKey+" and "+ s.toString(), dClass);
        query.setParameter(entityIdKey, entity.getId());
        for (String key : similarMap.keySet()) {
            query.setParameter(key, similarMap.get(key));
        }
        return query.list();
    }
}
