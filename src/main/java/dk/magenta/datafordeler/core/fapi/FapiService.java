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

    @WebMethod
    @GET
    @Path("{id}")
    @Produces("application/xml,application/json")
    public E get(@WebParam(name="id") @PathParam("id") @XmlElement(required=true) String id) {
        try {
            E entity = this.searchById(id);
            return entity;
        } catch (IllegalArgumentException e) {
            throw new InvalidClientInputException(e.getMessage());
        }
    }

    protected abstract Q getQuery(MultivaluedMap<String, String> parameters);

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
    protected E searchById(String id) {
        return this.searchById(UUID.fromString(id));
    }

    @WebMethod(exclude = true)
    protected E searchById(UUID uuid) {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        return this.queryManager.getEntity(session, uuid, this.getEntityClass());
    }

}
