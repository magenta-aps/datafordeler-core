package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataOutputException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lars on 19-04-17.
 */
@Component
public abstract class FapiService<E extends Entity, Q extends Query> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private QueryManager queryManager;

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGESIZE = "pageSize";
    public static final String PARAM_REGISTERFROM = "registerFrom";
    public static final String PARAM_REGISTERTO = "registerTo";


    @WebMethod(exclude = true)
    public abstract int getVersion();

    @WebMethod(exclude = true)
    public abstract String getServiceName();

    @WebMethod(exclude = true)
    protected abstract Class<E> getEntityClass();

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    private Logger log = LogManager.getLogger("FapiService");

    protected Logger getLogger() {
        return this.log;
    }

    @GET
    @Path("{id}")
    @Produces("application/xml,application/json")
    @WebMethod(exclude = true)
    public E getRest(@PathParam(value = "id") String id, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        Q query = this.getQuery(parameters);
        try {
            E entity = this.searchById(id, query);
            return entity;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidClientInputException(e.getMessage());
        }
    }

    @WebMethod(operationName = "get")
    public E getSoap(@WebParam(name="id") @XmlElement(required=true) String id,
                     @WebParam(name="registerFrom") @XmlElement(required = false) String registerFrom,
                     @WebParam(name="registerTo") @XmlElement(required = false) String registerTo) {
        Q query = this.getQuery(registerFrom, registerTo);
        try {
            E entity = this.searchById(id, query);
            return entity;
        } catch (IllegalArgumentException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
    }

    protected abstract Q getQuery();

    protected Q getQuery(MultivaluedMap<String, String> parameters) {
        Q query = this.getQuery();
        query.setPage(parameters.getFirst(PARAM_PAGE));
        query.setPageSize(parameters.getFirst(PARAM_PAGESIZE));
        query.setRegistrationFrom(parameters.getFirst(PARAM_REGISTERFROM));
        query.setRegistrationTo(parameters.getFirst(PARAM_REGISTERTO));
        return query;
    }

    private Q getQuery(String registrationFrom, String registrationTo) {
        Q query = this.getQuery();
        query.setRegistrationFrom(registrationFrom);
        query.setRegistrationTo(registrationTo);
        return query;
    }

    @GET
    @Path("search")
    @Produces("application/xml,application/json")
    @WebMethod(exclude = true)
    public String searchRest(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        Set<E> results = this.searchByQuery(this.getQuery(parameters));
        try {
            return this.objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            this.getLogger().error("Error outputting JSON: ", e);
            throw new DataOutputException(e);
        }
    }


    @WebMethod(operationName = "search")
    public String searchSoap(@WebParam(name="query") @XmlElement(required = true) Q query) {
        try {
            return this.objectMapper.writeValueAsString(this.searchByQuery(query));
        } catch (JsonProcessingException e) {
            this.getLogger().error("Error outputting JSON: ", e);
            throw new DataOutputException(e);
        }
    }

    @WebMethod(exclude = true)
    protected abstract Set<E> searchByQuery(Q query);

    @WebMethod(exclude = true)
    protected E searchById(String id, Q query) {
        return this.searchById(UUID.fromString(id), query);
    }

    @WebMethod(exclude = true)
    protected E searchById(UUID uuid, Q query) {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        if (query != null && query.getRegistrationFrom() != null) {
            System.out.println("activating filter");
            Filter filter = session.enableFilter("registrationFromFilter");
            filter.setParameter("registrationFromDate", query.getRegistrationFrom());
        }
        E entity = this.queryManager.getEntity(session, uuid, this.getEntityClass());
        entity.setFilter(query);
        return entity;
    }

}
