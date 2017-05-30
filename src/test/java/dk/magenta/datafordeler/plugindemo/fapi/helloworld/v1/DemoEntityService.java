package dk.magenta.datafordeler.plugindemo.fapi.helloworld.v1;

import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.plugindemo.fapi.DemoQuery;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lars on 19-04-17.
 */
@Path("")
@Component
@WebService
public class DemoEntityService extends FapiService<DemoEntity, DemoQuery> {

    @Override
    @WebMethod(exclude = true) // Non-soap methods must have this
    public int getVersion() {
        return 1;
    }

    @Override
    @WebMethod(exclude = true) // Non-soap methods must have this
    public String getServiceName() {
        return "postnummer";
    }

    @Override
    protected Class<DemoEntity> getEntityClass() {
        return DemoEntity.class;
    }

    @Override
    protected DemoQuery getEmptyQuery() {
        return new DemoQuery();
    }

    @WebMethod(exclude = true) // Non-soap methods must have this
    protected Set<DemoEntity> searchByQuery(DemoQuery query) {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        this.applyQuery(session, query);
        Set<DemoEntity> entities = null;
        try {
            entities = new HashSet<>(this.getQueryManager().getAllEntities(session, query, this.getEntityClass()));
        } catch (DataFordelerException e) {
            session.close();
            return null;
        }
        session.close();
        return entities;
    }

}
