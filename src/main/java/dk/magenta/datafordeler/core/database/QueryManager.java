package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import java.lang.reflect.Field;
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
        org.hibernate.query.Query<Identification> databaseQuery = session.createQuery("select i from Identification i where i.uuid = :uuid", Identification.class);
        databaseQuery.setParameter("uuid", uuid);
        this.logQuery(databaseQuery);
        try {
            return databaseQuery.getSingleResult();
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
        org.hibernate.query.Query<E> databaseQuery = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.uuid != null", eClass);
        this.logQuery(databaseQuery);
        List<E> results = databaseQuery.getResultList();
        return results;
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
     * @param query Query object defining search parameters
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity> List<E> getAllEntities(Session session, Query query, Class<E> eClass) throws DataFordelerException {
        this.log.info("Get all Entities of class " + eClass.getCanonicalName() + " matching parameters " + query.getSearchParameters() + " [offset: " + query.getOffset() + ", limit: " + query.getCount() + "]");
        StringJoiner queryString = new StringJoiner(" and ");
        Map<String, Object> parameters = query.getSearchParameters();
        for (String key : parameters.keySet()) {
            Object value = parameters.get(key);
            if (value != null) {
                if (parameterValueWildcard(value)) {
                    queryString.add("cast(d." + key + " as string) like :" + key);
                } else {
                    queryString.add("d." + key + " = :" + key);
                }
            }
        }
        if (queryString.length() > 0) {

            // Build query
            org.hibernate.query.Query<E> databaseQuery = session.createQuery("select e from " + eClass.getName() + " e join e.identification i join e.registrations r join r.effects v join v.dataItems d where i.uuid != null and " + queryString.toString(), eClass);

            // Insert parameters, casting as necessary
            for (String key : parameters.keySet()) {
                Object value = parameters.get(key);
                if (value != null) {
                    if (parameterValueWildcard(value)) {
                        databaseQuery.setParameter(key, ((String) value).replace("*", "%"));
                    } else {
                        try {
                            Field field = query.getField(key);
                            if (field.isAnnotationPresent(QueryField.class)) {
                                QueryField.FieldType type = field.getAnnotation(QueryField.class).type();
                                if (type == QueryField.FieldType.INT && !(value instanceof Integer)) {
                                    value = Integer.parseInt((String) value);
                                } else if (type == QueryField.FieldType.BOOLEAN && !(value instanceof Boolean)) {
                                    value = Query.booleanFromString((String) value);
                                }
                            }
                            databaseQuery.setParameter(key, value);

                        } catch (NoSuchFieldException e) {
                            throw new PluginImplementationException("Field "+key+" is missing from query class "+query.getClass().getCanonicalName()+", but defined in getSearchParameters() on that class", e);
                        }
                    }
                }
            }

            // Offset & limit
            if (query.getOffset() > 0) {
                databaseQuery.setFirstResult(query.getOffset());
            }
            if (query.getCount() < Integer.MAX_VALUE) {
                databaseQuery.setMaxResults(query.getCount());
            }
            this.logQuery(databaseQuery);
            List<E> results = databaseQuery.getResultList();
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
        org.hibernate.query.Query<E> databaseQuery = session.createQuery("select e from " + eClass.getName() + " e join e.identification i where i.uuid = :uuid", eClass);
        databaseQuery.setParameter("uuid", uuid);
        this.logQuery(databaseQuery);
        try {
            return databaseQuery.getSingleResult();
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
        org.hibernate.query.Query<V> databaseQuery = session.createQuery("select v from " + entity.getClass().getName() + " e join e.registrations r join r.effects v where e.id = :id and v.effectFrom = :from and v.effectTo = :to", vClass);
        databaseQuery.setParameter("id", entity.getId());
        databaseQuery.setParameter("from", effectFrom);
        databaseQuery.setParameter("to", effectTo);
        this.logQuery(databaseQuery);
        return databaseQuery.list();
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
        org.hibernate.query.Query<D> query = session.createQuery("select d from " + entity.getClass().getName() + " e join e.registrations r join r.effects v join v.dataItems d where e.id = :"+entityIdKey+" and "+ s.toString(), dClass);
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
        this.log.info("Saving registration of type "+registration.getClass().getCanonicalName()+" with checksum "+registration.getRegisterChecksum()+" and sequence number "+registration.getSequenceNumber());
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

        // Normalize references: setting them to existing Identification entries if possible
        // If no existing Identification exists, keep the one we have and save it to the session
        for (V effect : registration.getEffects()) {
            for (D dataItem : effect.getDataItems()) {
                HashMap<String, Identification> references = dataItem.getReferences();
                boolean changed = false;
                for (String key : references.keySet()) {
                    Identification reference = references.get(key);
                    if (reference != null) {
                        Identification otherReference = this.getIdentification(session, reference.getUuid());
                        if (otherReference != null && reference != otherReference) {
                            references.put(key, otherReference);
                            changed = true;
                        } else {
                            session.saveOrUpdate(reference);
                        }
                    }
                }
                if (changed) {
                    dataItem.updateReferences(references);
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
                /*for (Identification reference : dataItem.getReferences()) {
                    System.out.println("saving reference "+reference);
                    session.saveOrUpdate(reference);
                }*/
            }
        }
    }

    private void logQuery(org.hibernate.query.Query query) {
        if (this.log.isDebugEnabled()) {
            StringJoiner sj = new StringJoiner(", ");
            for (Parameter parameter : query.getParameters()) {
                sj.add(parameter.getName() + ": " + query.getParameterValue(parameter));
            }
            this.log.debug(query.getQueryString() + " [" + sj.toString() + "]");
        }
    }
}