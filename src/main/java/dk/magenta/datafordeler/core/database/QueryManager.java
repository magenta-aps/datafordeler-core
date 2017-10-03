package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.util.DoubleHashMap;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
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

    Logger log = LogManager.getLogger(QueryManager.class);

    public static final String ENTITY = "e";

    /**
     * Get one Identification object based on a UUID
     * @param session Database session to work from
     * @param uuid UUID to search for
     * @return
     */
    public Identification getIdentification(Session session, UUID uuid) {
        this.log.trace("Get Identification from UUID " + uuid);
        //Identification identification = session.get(Identification.class, uuid);
        //this.log.info(identification);
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
        this.log.trace("Get all Entities of class " + eClass.getCanonicalName());
        org.hibernate.query.Query<E> databaseQuery = session.createQuery("select "+ENTITY+" from " + eClass.getCanonicalName() + " " + ENTITY + " join "+ENTITY+".identification i where i.uuid != null", eClass);
        this.logQuery(databaseQuery);
        List<E> results = databaseQuery.getResultList();
        return results;
    }

    /**
     * Get all Entities of a specific class, that match the given parameters
     * @param session Database session to work from
     * @param query Query object defining search parameters
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity, D extends DataItem> List<E> getAllEntities(Session session, Query query, Class<E> eClass) throws DataFordelerException {
        this.log.info("Get all Entities of class " + eClass.getCanonicalName() + " matching parameters " + query.getSearchParameters() + " [offset: " + query.getOffset() + ", limit: " + query.getCount() + "]");

        LookupDefinition lookupDefinition = query.getLookupDefinition();
        String root = "d";

        String extraWhere = lookupDefinition.getHqlWhereString(root, ENTITY);

        String queryString = "SELECT DISTINCT "+ENTITY+" from " + eClass.getCanonicalName() + " " + ENTITY +
                " WHERE " + ENTITY + ".identification.uuid IS NOT null "+ extraWhere;

        this.log.info(queryString);

        // Build query
        org.hibernate.query.Query<E> databaseQuery = session.createQuery(queryString, eClass);

        // Insert parameters, casting as necessary
        HashMap<String, Object> extraParameters = lookupDefinition.getHqlParameters(root, ENTITY);

        for (String key : extraParameters.keySet()) {
            this.log.info(key+" = "+extraParameters.get(key));
            databaseQuery.setParameter(key, extraParameters.get(key));
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
    }

    /**
     * Get one Entity of a specific class, by uuid
     * @param session Database session to work from
     * @param uuid UUID to search for
     * @param eClass Entity subclass
     * @return
     */
    public <E extends Entity> E getEntity(Session session, UUID uuid, Class<E> eClass) {
        this.log.trace("Get Entity of class " + eClass.getCanonicalName() + " by uuid "+uuid);
        org.hibernate.query.Query<E> databaseQuery = session.createQuery("select "+ENTITY+" from " + eClass.getCanonicalName() + " " + ENTITY + " join "+ENTITY+".identification i where i.uuid = :uuid", eClass);
        databaseQuery.setParameter("uuid", uuid);
        this.logQuery(databaseQuery);
        try {
            return databaseQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public <T extends DatabaseEntry> List<T> getItems(Session session, Class<T> tClass, Map<String, Object> filter) {
        StringJoiner whereJoiner = new StringJoiner(" and ");
        for (String key : filter.keySet()) {
            whereJoiner.add("t."+key+" = :"+key);
        }
        org.hibernate.query.Query<T> databaseQuery = session.createQuery("select t from " + tClass.getCanonicalName() + " t where " + whereJoiner.toString(), tClass);
        for (String key : filter.keySet()) {
            databaseQuery.setParameter(key, filter.get(key));
        }
        return databaseQuery.getResultList();
    }

    public <T extends DatabaseEntry> T getItem(Session session, Class<T> tClass, Map<String, Object> filter) {
        List<T> items = this.getItems(session, tClass, filter);
        if (!items.isEmpty()) {
            return items.get(0);
        } else {
            return null;
        }
    }

    public <T extends DatabaseEntry> long count(Session session, Class<T> tClass, Map<String, Object> filter) {
        String where = "";
        if (filter != null && !filter.isEmpty()) {
            StringJoiner whereJoiner = new StringJoiner(" and ");
            for (String key : filter.keySet()) {
                whereJoiner.add("t." + key + " = :" + key);
            }
            where = " where " + whereJoiner.toString();
        }
        org.hibernate.query.Query databaseQuery = session.createQuery("select count(t) from " + tClass.getCanonicalName() + " t " + where);
        if (filter != null && !filter.isEmpty()) {
            for (String key : filter.keySet()) {
                databaseQuery.setParameter(key, filter.get(key));
            }
        }
        return (long) databaseQuery.uniqueResult();
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
        this.log.trace("Get Effects of class " + vClass.getCanonicalName() + " under Entity "+entity.getUUID() + " from "+effectFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + " to " + effectTo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        org.hibernate.query.Query<V> databaseQuery = session.createQuery("select v from " + entity.getClass().getCanonicalName() + " " + ENTITY +" join "+ENTITY+".registrations r join r.effects v where "+ENTITY+".id = :id and v.effectFrom = :from and v.effectTo = :to", vClass);
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
    public <D extends DataItem> List<D> getDataItems(Session session, Entity entity, D similar, Class<D> dClass) throws PluginImplementationException {
        this.log.debug("Get DataItems of class " + dClass.getCanonicalName() + " under Entity "+entity.getUUID() + " with content matching DataItem "+similar.asMap());
        LookupDefinition lookupDefinition = similar.getLookupDefinition();
        String dataItemKey = "d";
        String extraJoin = lookupDefinition.getHqlJoinString(dataItemKey, ENTITY);
        StringJoiner extraWhere = new StringJoiner(" AND ");

        List<String> whereParts = lookupDefinition.getHqlWhereParts(dataItemKey, ENTITY, true);
        if (!whereParts.isEmpty()) {
            extraWhere.add("");
            for (String part : whereParts) {
                extraWhere.add(part);
            }
        }

        String entityIdKey = "E" + UUID.randomUUID().toString().replace("-", "");

        String queryString = "SELECT " + dataItemKey + " FROM " + dClass.getCanonicalName() + " " + dataItemKey +
                " JOIN " + dataItemKey+".effects v" +
                " JOIN v.registration r" +
                " JOIN r.entity "+ENTITY + " "+extraJoin +
                " WHERE " + ENTITY + ".id = :"+entityIdKey + " "+ extraWhere.toString();


        this.log.debug(queryString);
        org.hibernate.query.Query<D> query = session.createQuery(queryString, dClass);

        query.setParameter(entityIdKey, entity.getId());
        HashMap<String, Object> extraParameters = lookupDefinition.getHqlParameters(dataItemKey, ENTITY);
        for (String key : extraParameters.keySet()) {
            query.setParameter(key, extraParameters.get(key));
        }
        this.logQuery(query);
        List<D> results = query.list();
        return results;
    }

    /**
     * Identify and remove duplicate Effects in a Registration, so the Registration will only have unique Effects
     * @param session Database session to work from
     * @param registration Registration to dedup
     */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void dedupEffects(Session session, R registration) {
        this.log.trace("Remove duplicate Effects in Registration " + registration.getId() + " ("+registration.getRegisterChecksum()+")");
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
            this.log.info("No duplicate effects found");
        } else {
            for (V master : duplicates.keySet()) {
                List<V> dups = duplicates.get(master);
                this.log.info("There are " + dups.size() + " duplicates of Effect " + master.getEffectFrom() + " - " + master.getEffectTo());
                int i = 0;
                for (V dup : dups) {
                    this.log.info("    Duplicate " + i + " contains " + dup.getDataItems().size() + " DataItems");
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
        this.saveRegistration(session, entity, registration, true, true);
    }

    /**
     * Save registration to database, re-pointing the entity reference to a persistent entity if one exists, merging effects with identical timestamps, and saving all associated effects and dataitems.
     * @param session A database session to work on
     * @param registration Registration to be saved
     */
    public <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void saveRegistration(Session session, E entity, R registration, boolean dedupEffects, boolean dedupItems) throws DataFordelerException {
        this.log.info("Saving registration of type " + registration.getClass().getCanonicalName() + " with checksum " + registration.getRegisterChecksum() + " and sequence number " + registration.getSequenceNumber());
        if (entity == null && registration.entity != null) {
            entity = registration.entity;
        }
        if (entity == null) {
            throw new MissingEntityException(registration);
        }

        E existingEntity = this.getEntity(session, entity.getUUID(), (Class<E>) entity.getClass());
        if (existingEntity != null) {
            this.log.info("There is an existing entity with uuid "+existingEntity.getUUID().toString());
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

        if (dedupItems) {
            dedupItems(session, entity, registration);
        }

        Identification existing;
        if (entity.getIdentification() != null && entity.getIdentification().getId() != null) {
            existing = session.get(Identification.class, entity.getIdentification().getId());
        } else {
            existing = this.getIdentification(session, entity.getUUID());
        }
        if (existing != null && existing != entity.getIdentification()) {
            this.log.debug("identification "+entity.getUUID()+" already exist");
            entity.setIdentifikation(existing);
            session.saveOrUpdate(entity);
        } else if (existing == null) {
            this.log.debug("identification "+entity.getUUID()+" does not already exist or is already assigned");
            session.saveOrUpdate(entity.getIdentification());
            session.saveOrUpdate(entity);
        }



        if (dedupEffects) {
            this.dedupEffects(session, registration);
        }

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

    private <E extends Entity<E, R>, R extends Registration<E, R, V>, V extends Effect<R, V, D>, D extends DataItem<V, D>> void dedupItems(Session session, E entity, R registration) throws PluginImplementationException {
        // Dedup dataitems
        // Find existing DataItems on the Entity that hold the same data
        for (V effect : registration.getEffects()) {
            HashSet<D> obsolete = new HashSet<D>();
            for (D dataItem : effect.getDataItems()) {
                List<D> existing = this.getDataItems(session, entity, dataItem, (Class<D>) dataItem.getClass());
                // If found, use that DataItem instead
                if (existing != null && !existing.isEmpty() && dataItem != existing.get(0)) {
                    obsolete.add(dataItem);
                    dataItem = existing.get(0);
                }
                // Couple it with the Effect
                dataItem.addEffect(effect);
            }

            this.log.info("Found " + obsolete.size() + " obsolete items!");
            for (D dataItem : obsolete) {
                Set<V> effects = dataItem.getEffects();
                for (V e : effects) {
                    e.dataItems.remove(dataItem);
                    session.saveOrUpdate(e);
                }
                session.delete(dataItem);
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
