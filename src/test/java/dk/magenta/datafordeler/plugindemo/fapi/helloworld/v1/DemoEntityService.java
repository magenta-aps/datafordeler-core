package dk.magenta.datafordeler.plugindemo.fapi.helloworld.v1;

import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.fapi.DemoQuery;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by lars on 19-04-17.
 */
@Controller
@RequestMapping("/demo/postnummer/1/rest")
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

    @Override
    protected void checkAccess(DafoUserDetails user)
        throws AccessDeniedException, AccessRequiredException {
        if(user == null) {
            throw new AccessRequiredException(
                "You must provide a DAFO token to use this service"
            );
        }

        // Check that user has access to the service and to the necessary entity
        user.checkHasSystemRole(DemoRolesDefinition.READ_SERVICE_ROLE);
        user.checkHasSystemRole(DemoRolesDefinition.READ_DEMO_ENTITY_ROLE);
    }

    @WebMethod(exclude = true) // Non-soap methods must have this
    protected Set<DemoEntity> searchByQuery(DemoQuery query) throws AccessDeniedException {
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
