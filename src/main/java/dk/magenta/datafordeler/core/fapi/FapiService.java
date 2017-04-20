package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.Entity;
import org.springframework.beans.factory.annotation.Autowired;

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

/**
 * Created by lars on 19-04-17.
 */
public abstract class FapiService<E extends Entity, Q extends Query> {

    @Autowired
    protected ObjectMapper objectMapper;

    @WebMethod(exclude = true)
    public abstract int getVersion();

    @WebMethod(exclude = true)
    public abstract String getServiceName();

    @WebMethod
    @GET
    @Path("{id}")
    @Produces("application/json")
    public String get(@WebParam(name="id") @PathParam("id") @XmlElement(required=true) String id) {
        try {
            E entity = this.searchById(id);
            return this.objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            return "FAIL";
        }
    }

    protected abstract Q getQuery(MultivaluedMap<String, String> parameters);

    @GET
    @Path("search")
    @Produces("application/json")
    @WebMethod(exclude = true)
    public String searchRest(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        Set<E> results = this.searchByQuery(this.getQuery(parameters));
        try {
            return this.objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            return "FAIL";
        }
    }


    @WebMethod(operationName = "search")
    public String searchSoap(@WebParam(name="query") @XmlElement(required = true) Q query) {
        try {
            return this.objectMapper.writeValueAsString(this.searchByQuery(query));
        } catch (JsonProcessingException e) {
            return "FAIL";
        }
    }

    @WebMethod(exclude = true)
    protected abstract Set<E> searchByQuery(Q query);

    @WebMethod(exclude = true)
    protected abstract E searchById(String id);

}
