package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by lars on 22-02-17.
 */
@Component
public class QueryManager {

    Logger log = LogManager.getLogger("QueryManager");

    /**
     * Get one Identification object based on a UUID
     * @param session Database session to work from
     * @param uuid UUID to search for
     * @return
     */
    public Identification getIdentification(Session session, UUID uuid) {
        this.log.info("Get Identification from UUID " + uuid.toString());
        Query<Identification> query = session.createQuery("select i from Identification i where i.uuid = :uuid", Identification.class);
        query.setParameter("uuid", uuid);
        this.logQuery(query);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get all Entities of a specific class
     * @param session Database session to work from
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity> List<E> getAllEntities(Session session, Class<E> eClass) {
        this.log.info("Get all Entities of class " + eClass.getCanonicalName());
        Query<E> query = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.uuid != null", eClass);
        this.logQuery(query);
        List<E> results = query.getResultList();
        return results;
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * @param session Database session to work from
     * @param parameters Map of parameters to search by
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity> List<E> getAllEntities(Session session, Map<String, Object> parameters, Class<E> eClass) {
        return this.getAllEntities(session, parameters, 0, Integer.MAX_VALUE, eClass);
    }

    private static boolean parameterValueWildcard(Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            return stringValue.startsWith("*") || stringValue.endsWith("*");
        }
        return false;
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * @param session Database session to work from
     * @param parameters Map of parameters to search by
     * @param offset Result offset
     * @param limit Result limit
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity> List<E> getAllEntities(Session session, Map<String, Object> parameters, int offset, int limit, Class<E> eClass) {
        this.log.info("Get all Entities of class " + eClass.getCanonicalName() + " matching parameters " + parameters + " [offset: " + offset + ", limit: " + limit + "]");
        StringJoiner queryString = new StringJoiner(" and ");
        for (String key : parameters.keySet()) {
            Object value = parameters.get(key);
            if (value != null) {
                if (parameterValueWildcard(value)) {
                    queryString.add("d." + key + " like :" + key);
                } else {
                    queryString.add("d." + key + " = :" + key);
                }
            }
        }
        if (queryString.length() > 0) {
            Query<E> query = session.createQuery("select e from " + eClass.getName() + " e join e.identification i join e.registrations r join r.effects v join v.dataItems d where i.uuid != null and " + queryString.toString(), eClass);
            for (String key : parameters.keySet()) {
                Object value = parameters.get(key);
                if (value != null) {
                    if (parameterValueWildcard(value)) {
                        query.setParameter(key, ((String) value).replace("*", "%"));
                    } else {
                        query.setParameter(key, value);
                    }
                }
            }
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            if (limit < Integer.MAX_VALUE) {
                query.setMaxResults(limit);
            }
            this.logQuery(query);
            List<E> results = query.getResultList();
            return results;
        } else {
            // Throw error?
            return Collections.emptyList();
        }
    }

    /**
     * Get one Entity of a specific class, by uuid
     * @param session Database session to work from
     * @param uuid UUID to search for
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity> E getEntity(Session session, UUID uuid, Class<E> eClass) {
        this.log.info("Get Entity of class " + eClass.getCanonicalName() + " by uuid "+uuid.toString());
        Query<E> query = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.uuid = :uuid", eClass);
        query.setParameter("uuid", uuid);
        this.logQuery(query);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get Effects for a specific class, matching a start and end time
     * @param session Database session to work from
     * @param entity Entity to search under
     * @param effectFrom start time
     * @param effectTo end time
     * @param vClass Effect subclass
     * @return
     */
    public <V extends Effect> List<V> getEffects(Session session, Entity entity, OffsetDateTime effectFrom, OffsetDateTime effectTo, Class<V> vClass) {
        // AFAIK, this method is only ever used for testing
        this.log.info("Get Effects of class " + vClass.getCanonicalName() + " under Entity "+entity.getUUID() + " from "+effectFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + " to " + effectTo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        Query<V> query = session.createQuery("select v from " + entity.getClass().getName() + " e join e.registrations r join r.effects v where e.id = :id and v.effectFrom = :from and v.effectTo = :to", vClass);
        query.setParameter("id", entity.getId());
        query.setParameter("from", effectFrom);
        query.setParameter("to", effectTo);
        this.logQuery(query);
        return query.list();
    }

    /**
     * Get DataItems matching an existing DataItem (for locating duplicates)
     * @param session Database session to work from
     * @param entity Entity to search under
     * @param similar Another DataItem, whose properties will form the base of the search
     * @param dClass DataItem subclass
     * @return
     */
    public <D extends DataItem> List<D> getDataItems(Session session, Entity entity, D similar, Class<D> dClass) {
        this.log.info("Get DataItems of class " + dClass.getCanonicalName() + " under Entity "+entity.getUUID() + " with content maching DataItem "+similar.asMap());
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
        this.logQuery(query);
        return query.list();
    }

    /**
     * Identify and remove duplicate Effects in a Registration, so the REgistration will only have unique Effects
     * @param session Database session to work from
     * @param registration Registration to dedup
     */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void dedupEffects(Session session, R registration) {
        this.log.info("Remove duplicate Effects in Registration " + registration.getId() + " ("+registration.getRegisterChecksum()+")");
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
        if (duplicates.isEmpty()) {
            this.log.debug("No duplicates found");
        } else {
            for (V master : duplicates.keySet()) {
                List<V> dups = duplicates.get(master);
                this.log.debug("There are " + dups.size() + " duplicates of Effect " + master.getEffectFrom() + " - " + master.getEffectTo());
                int i = 0;
                for (V dup : dups) {
                    this.log.debug("    Duplicate " + i + " contains " + dup.getDataItems().size() + " DataItems");
                    for (D dataItem : dup.getDataItems()) {
                        dataItem.addEffect(master);
                        dataItem.removeEffect(dup);
                    }
                    registration.removeEffect(dup);
                    session.delete(dup);
                    i++;
                }
            }
        }
    }

    /**
     * Save registration to database, re-pointing the entity reference to a persistent entity if one exists, merging effects with identical timestamps, and saving all associated effects and dataitems.
     * @param session A database session to work on
     * @param registration Registration to be saved
     */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void saveRegistration(Session session, E entity, R registration) throws DataFordelerException {
        this.log.info("Saving registration with checksum "+registration.getRegisterChecksum()+" and sequence number "+registration.getSequenceNumber());
        if (entity == null && registration.entity != null) {
            entity = registration.entity;
        }
        if (entity == null) {
            throw new MissingEntityException(registration);
        }
        E existingEntity = this.getEntity(session, entity.getUUID(), (Class<E>) entity.getClass());
        if (existingEntity != null) {
            this.log.debug("There is an existing entity with uuid "+existingEntity.getUUID().toString());
            entity = existingEntity;
        }
        registration.setEntity(entity);
        entity.addRegistration(registration);

        // Validate registration:
        // * No existing registration on the entity may have the same sequence number
        // * The registration must have a sequence number one higher than the highest existing one
        int highestSequenceNumber = -1;
        R lastExistingRegistration = null;
        if (registration.getId() == null) {
            // New registration
            for (R otherRegistration : entity.getRegistrations()) {
                if (otherRegistration != registration) { // Consider only other registrations
                    if (otherRegistration.getId() != null || session.contains(otherRegistration)) { // Consider only saved registrations
                        if (otherRegistration.getSequenceNumber() == registration.getSequenceNumber()) {
                            throw new DuplicateSequenceNumberException(registration, otherRegistration);
                        }
                        if (otherRegistration.getSequenceNumber() > highestSequenceNumber) {
                            highestSequenceNumber = otherRegistration.getSequenceNumber();
                            lastExistingRegistration = otherRegistration;
                        }
                    }
                }
            }
            if (highestSequenceNumber > -1 && registration.getSequenceNumber() != highestSequenceNumber + 1) {
                throw new SkippedSequenceNumberException(registration, highestSequenceNumber);
            }
            if (lastExistingRegistration != null) {
                if (lastExistingRegistration.getRegistrationTo() == null) {
                    lastExistingRegistration.setRegistrationTo(registration.getRegistrationFrom());
                } else if (!lastExistingRegistration.getRegistrationTo().equals(registration.getRegistrationFrom())) {
                    throw new MismatchingRegistrationBoundaryException(registration, lastExistingRegistration);
                }
            }
        }


        for (V effect : registration.getEffects()) {
            HashSet<D> obsolete = new HashSet<D>();
            for (D dataItem : effect.getDataItems()) {
                // Find existing DataItems on the Entity that hold the same data
                List<D> existing = this.getDataItems(session, entity, dataItem, (Class<D>) dataItem.getClass());
                // If found, use that DataItem instead
                if (existing != null && !existing.isEmpty() && dataItem != existing.get(0)) {
                    obsolete.add(dataItem);
                    dataItem = existing.get(0);
                }
                // Couple it with the Effect
                dataItem.addEffect(effect);
            }
            for (D dataItem : obsolete) {
                session.delete(dataItem);
                dataItem.removeEffect(effect);
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
        if (lastExistingRegistration != null) {
            session.update(lastExistingRegistration);
        }
        for (V effect : registration.getEffects()) {
            session.saveOrUpdate(effect);
            for (D dataItem : effect.getDataItems()) {
                session.saveOrUpdate(dataItem);
            }
        }
    }

    private void logQuery(Query query) {
        if (this.log.isDebugEnabled()) {
            StringJoiner sj = new StringJoiner(", ");
            for (Parameter parameter : query.getParameters()) {
                sj.add(parameter.getName() + ": " + query.getParameterValue(parameter));
            }
            this.log.debug(query.getQueryString() + " [" + sj.toString() + "]");
        }
    }
}
