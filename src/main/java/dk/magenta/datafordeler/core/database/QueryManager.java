package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 22-02-17.
 */
@Component
public class QueryManager {

    Logger log = LogManager.getLogger("Database");

    public Identification getIdentification(Session session, UUID uuid) {
        Query<Identification> query = session.createQuery("select i from Identification i where i.uuid = :uuid", Identification.class);
        query.setParameter("uuid", uuid);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <E extends Entity> List<E> getAllEntities(Session session, Class<E> eClass) {
        Query<E> query = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.uuid != null", eClass);
        List<E> results = query.getResultList();
        return results;
    }

    public <E extends Entity> E getEntity(Session session, UUID uuid, Class<E> eClass) {
        Query<E> query = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.uuid = :uuid", eClass);
        query.setParameter("uuid", uuid);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <V extends Effect> List<V> getEffects(Session session, Entity entity, OffsetDateTime effectFrom, OffsetDateTime effectTo, Class<V> vClass) {
        Query<V> query = session.createQuery("select v from " + entity.getClass().getName() + " e join e.registrations r join r.effects v where e.id = :id and v.effectFrom = :from and v.effectTo = :to", vClass);
        query.setParameter("id", entity.getId());
        query.setParameter("from", effectFrom);
        query.setParameter("to", effectTo);
        return query.list();
    }

    /*public <D extends DataItem> List<D> getDataItems(Session session, Entity entity, OffsetDateTime effectFrom, OffsetDateTime effectTo, Class<D> dClass) {
        Query<D> query = session.createQuery("select d from " + entity.getClass().getName() + " e join e.registrationReferences r join r.effects v join v.dataItems d where e.id = :id and v.effectFrom = :from and v.effectTo = :to", dClass);
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

    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void dedupEffects(Session session, R registration) {
        DoubleHashMap<OffsetDateTime, OffsetDateTime, V> authoritative = new DoubleHashMap<>();
        ListHashMap<V, V> duplicates = new ListHashMap<>();
        for (V effect : registration.getEffects()) {
            OffsetDateTime key1 = effect.getEffectFrom();
            OffsetDateTime key2 = effect.getEffectTo();
            if (!authoritative.containsKey(key1, key2)) {
                authoritative.put(key1, key2, effect);
            } else {
                duplicates.add(authoritative.get(key1, key2), effect);
            }
        }
        if (!duplicates.isEmpty()) {
            for (V master : duplicates.keySet()) {
                List<V> dups = duplicates.get(master);
                log.debug(dups.size()+" duplicates of effect "+master.getEffectFrom()+" - "+master.getEffectTo());
                for (V dup : dups) {
                    log.debug("Duplicate contains " + dup.getDataItems().size() + " dataitems");
                    for (D dataItem : dup.getDataItems()) {
                        dataItem.addEffect(master);
                        dataItem.removeEffect(dup);
                    }
                    registration.removeEffect(dup);
                    session.delete(dup);
                }
            }
        }
    }

    /**
     * Save registration to database, re-pointing the entity reference to a persistent entity if one exists, merging effects with identical timestamps, and saving all associated effects and dataitems.
     * @param session A database session to work on
     * @param registration Registration to be saved
     */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void saveRegistration(Session session, R registration) {
        log.info("Saving registration with checksum "+registration.getRegisterChecksum());
        E entity = registration.getEntity();
        E existingEntity = this.getEntity(session, entity.getUUID(), (Class<E>) entity.getClass());
        if (existingEntity != null) {
            entity = existingEntity;
            registration.setEntity(entity);
        }
        for (V effect : registration.getEffects()) {
            for (D dataItem : effect.getDataItems()) {
                // Find existing DataItems on the Entity that hold the same data
                List<D> existing = this.getDataItems(session, entity, dataItem, (Class<D>) dataItem.getClass());
                // If found, use that DataItem instead
                if (existing != null && !existing.isEmpty()) {
                    dataItem = existing.get(0);
                }
                // Couple it with the Effect
                dataItem.addEffect(effect);
            }
        }

        Identification existing = this.getIdentification(session, entity.getUUID());
        if (existing != null && existing != entity.getIdentification()) {
            entity.setIdentification(existing);
        } else {
            session.saveOrUpdate(entity.getIdentification());
        }
        session.saveOrUpdate(entity);

        this.dedupEffects(session, registration);

        session.saveOrUpdate(registration);
        for (V effect : registration.getEffects()) {
            session.saveOrUpdate(effect);
            for (D dataItem : effect.getDataItems()) {
                session.saveOrUpdate(dataItem);
            }
        }
    }
}
