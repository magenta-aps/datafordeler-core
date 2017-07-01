package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import javax.servlet.http.HttpServletRequest;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lars on 19-04-17.
 */
@Component
@RestController
public abstract class FapiService<E extends Entity, Q extends Query> {

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGESIZE = "pageSize";
    public static final String PARAM_REGISTERFROM = "registerFrom";
    public static final String PARAM_REGISTERTO = "registerTo";
    public static final String PARAM_EFFECTFROM = "effectFrom";
    public static final String PARAM_EFFECTTO = "effectTo";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    @Context
    private MessageContext context;

    private Logger log = LogManager.getLogger("FapiService");


    /**
     * Obtains the version number of the service. This will be used in the path that requests may interface with
     * @return service version, e.g. 1
     */
    @WebMethod(exclude = true)
    public abstract int getVersion();


    /**
     * Obtains the name of the service. This will be used in the path that requests may interface with
     * @return service name, e.g. "postnummer"
     */
    @WebMethod(exclude = true)
    public abstract String getServiceName();


    /**
     * Obtains the Entity class that the service handles.
     * @return Entity subclass
     */
    @WebMethod(exclude = true)
    protected abstract Class<E> getEntityClass();


    /**
     * Obtains the autowired SessionManager
     * @return SessionManager instance
     */
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }


    /**
     * Obtains the autowired QueryManager
     * @return QueryManager instance
     */
    protected QueryManager getQueryManager() {
        return this.queryManager;
    }


    /**
     * Checks that the user has access to the service
     * @param user DafoUserDetails object representing the user provided from a SAML token.
     * @throws AccessDeniedException
     *
     * Implementing this method as a noop will make the service publicly accessible.
     */
    protected abstract void checkAccess(DafoUserDetails user)
        throws AccessDeniedException, AccessRequiredException;


    /**
     * Handle a lookup-by-UUID request in REST. This method is called by the Servlet
     * @param id Identifier coming from the client
     * @param requestParams url parameters
     * @return Found Entity, or null if none found.
     */
    //@GET
    //@Path("{id}")
    @Produces("application/xml,application/json")
    @WebMethod(exclude = true)
    @RequestMapping("{id}")
    public Envelope<E> getRest(@PathVariable("id") String id, @RequestParam MultiValueMap<String, String> requestParams, HttpServletRequest request)
        throws AccessDeniedException, AccessRequiredException, InvalidTokenException, InvalidClientInputException {
        this.log.info("Incoming REST request for item "+id); // TODO: add user from request
        Envelope<E> envelope = new Envelope<E>();
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        this.checkAccess(user);
        Q query = this.getQuery(requestParams, true);
        envelope.addQueryData(query);
        envelope.addUserData(user);
        envelope.addRequestData(request);
        try {
            E entity = this.searchById(id, query);
            envelope.setResult(entity);
            this.log.debug("Item found, returning");
            envelope.close();
            return envelope;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidClientInputException(e.getMessage());
        }
    }


    /**
     * Handle a lookup-by-UUID request in SOAP. This method is called by the Servlet
     * @param id Identifier coming from the client
     * @param registerFrom Low boundary for registration inclusion
     * @param registerTo High boundary for registration inclusion
     * @return Found Entity, or null if none found.
     */
    // TODO: How to use DafoUserDetails with SOAP requests?
    @WebMethod(operationName = "get")
    public Envelope<E> getSoap(@WebParam(name="id") @XmlElement(required=true) String id,
                     @WebParam(name="registerFrom") @XmlElement(required = false) String registerFrom,
                     @WebParam(name="registerTo") @XmlElement(required = false) String registerTo,
                     HttpServletRequest request)
        throws InvalidClientInputException {
        this.log.info("Incoming SOAP request for item "+id); // TODO: add user from request
        Envelope<E> envelope = new Envelope<E>();
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        Q query = this.getQuery(registerFrom, registerTo);
        envelope.addQueryData(query);
        envelope.addUserData(user);
        envelope.addRequestData(request);
        try {
            E entity = this.searchById(id, query);
            envelope.setResult(entity);
            this.log.debug("Item found, returning");
            this.log.info("registrations: "+entity.getRegistrations());
            envelope.close();
            return envelope;
        } catch (IllegalArgumentException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
    }


    /**
     * Parse a registration boundary into a Query object of the correct subclass
     * @param registrationFrom Low boundary for registration inclusion
     * @param registrationTo High boundary for registration inclusion
     * @return Query subclass instance
     */
    protected Q getQuery(String registrationFrom, String registrationTo) {
        Q query = this.getEmptyQuery();
        query.setRegistrationFrom(registrationFrom);
        query.setRegistrationTo(registrationTo);
        return query;
    }


    /**
     * Handle a lookup-by-parameters request in REST. This method is called by the Servlet
     * @param requestParams Request Parameters from spring boot
     * @return Found Entities
     */
    @Produces("application/xml,application/json")
    @WebMethod(exclude = true)
    @RequestMapping("search")
    public Envelope<E> searchRest(@RequestParam MultiValueMap<String, String> requestParams, HttpServletRequest request) throws DataFordelerException {
        this.log.info("Incoming REST request, searching for parameters "+requestParams.toString()); // TODO: add user from request
        Envelope<E> envelope = new Envelope<E>();
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        Q query = this.getQuery(requestParams, false);
        this.checkAccess(user);
        envelope.addQueryData(query);
        envelope.addUserData(user);
        envelope.addRequestData(request);
        Set<E> results = this.searchByQuery(query);
        envelope.setResults(results);
        this.log.info(results.size() + " items found, returning");
        envelope.close();
        return envelope;
    }


    /**
     * Handle a lookup-by-parameters request in SOAP. This method is called by the Servlet
     * @param query Query object specifying search parameters
     * @return Found Entities
     */
    // TODO: How to use DafoUserDetails with SOAP requests?
    @WebMethod(operationName = "search")
    public Envelope<E> searchSoap(@WebParam(name="query") @XmlElement(required = true) Q query, HttpServletRequest request) throws DataFordelerException {
        this.log.info("Incoming SOAP request, searching for query "+query.toString()); // TODO: add user from request
        Envelope<E> envelope = new Envelope<E>();
        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        envelope.addQueryData(query);
        envelope.addUserData(user);
        envelope.addRequestData(request);
        Set<E> results = this.searchByQuery(query);
        envelope.setResults(results);
        this.log.info(results.size() + " items found, returning");
        envelope.close();
        return envelope;
    }


    /**
     * Obtain an empty Query instance of the correct subclass
     * @return Query subclass instance
     */
    protected abstract Q getEmptyQuery();


    /**
     * Parse a map of URL parameters into a Query object of the correct subclass
     * @param parameters URL parameters received in a request
     * @return Query subclass instance
     */
    protected Q getQuery(MultiValueMap<String, String> parameters, boolean limitsOnly) {
        Q query = this.getEmptyQuery();
        if (!limitsOnly) {
            query.setFromParameters(new ParameterMap(parameters));
        }
        query.setPage(parameters.getFirst(PARAM_PAGE));
        query.setPageSize(parameters.getFirst(PARAM_PAGESIZE));
        query.setRegistrationFrom(parameters.getFirst(PARAM_REGISTERFROM));
        query.setRegistrationTo(parameters.getFirst(PARAM_REGISTERTO));
        query.setEffectFrom(parameters.getFirst(PARAM_EFFECTFROM));
        query.setEffectTo(parameters.getFirst(PARAM_EFFECTTO));
        return query;
    }


    /**
     * Perform a search for Entities by a Query object
     * @param query Query objects to search by
     * @return Found Entities
     */
    //@WebMethod(exclude = true)
    //protected abstract Set<E> searchByQuery(Q query);
    @WebMethod(exclude = true) // Non-soap methods must have this
    protected Set<E> searchByQuery(Q query) throws DataFordelerException {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        this.applyQuery(session, query);
        Set<E> entities = null;
        try {
            entities = new HashSet<>(this.getQueryManager().getAllEntities(session, query, this.getEntityClass()));
            for (E entity : entities) {
                entity.forceLoad(session);
            }
        } finally {
            transaction.commit();
            session.close();
        }
        return entities;
    }

    /**
     * Perform a search for Entities by id
     * @param id Identifier to search by. Must be parseable as a UUID
     * @param query Query object modifying the output (such as a bitemporal range)
     * @return Found Entities
     */
    @WebMethod(exclude = true)
    protected E searchById(String id, Q query) {
        return this.searchById(UUID.fromString(id), query);
    }


    /**
     * Perform a search for Entities by id
     * @param uuid Identifier to search by
     * @param query Query object modifying the output (such as a bitemporal range)
     * @return Found Entities
     */
    @WebMethod(exclude = true)
    protected E searchById(UUID uuid, Q query) {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        this.applyQuery(session, query);
        E entity = this.queryManager.getEntity(session, uuid, this.getEntityClass());
        entity.forceLoad(session);
        session.close();
        return entity;
    }


    /**
     * Put Query parameters into the Hibernate session. Subclasses should override this and call this method, then
     * put their own Query-subclass-specific parameters in as well
     * @param session Hibernate session in use
     * @param query Query object constructed from a request
     */
    protected void applyQuery(Session session, Q query) {
        if (query != null) {
            if (query.getRegistrationFrom() != null) {
                Filter filter = session.enableFilter(Registration.FILTER_REGISTRATION_FROM);
                filter.setParameter(Registration.FILTERPARAM_REGISTRATION_FROM, query.getRegistrationFrom());
            }
            if (query.getRegistrationTo() != null) {
                Filter filter = session.enableFilter(Registration.FILTER_REGISTRATION_TO);
                filter.setParameter(Registration.FILTERPARAM_REGISTRATION_TO, query.getRegistrationTo());
            }
            if (query.getEffectFrom() != null) {
                Filter filter = session.enableFilter(Effect.FILTER_EFFECT_FROM);
                filter.setParameter(Effect.FILTERPARAM_EFFECT_FROM, query.getEffectFrom());
            }
            if (query.getEffectTo() != null) {
                Filter filter = session.enableFilter(Effect.FILTER_EFFECT_TO);
                filter.setParameter(Effect.FILTERPARAM_EFFECT_TO, query.getEffectTo());
            }
        }
    }

}
