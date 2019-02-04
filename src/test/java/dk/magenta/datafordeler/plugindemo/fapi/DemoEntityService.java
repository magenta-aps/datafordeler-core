package dk.magenta.datafordeler.plugindemo.fapi;

import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.fapi.DemoQuery;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.jws.WebMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@RestController
@RequestMapping("/demo/postnummer/1/rest")
public class DemoEntityService extends FapiBaseService<DemoEntityRecord, DemoRecordQuery> {

    @Autowired
    private DemoPlugin demoPlugin;

    @Autowired
    private DemoRecordOutputWrapper demoRecordOutputWrapper;


    @PostConstruct
    public void init() {
        this.setOutputWrapper(this.demoRecordOutputWrapper);
    }

    @Override
    @WebMethod(exclude = true)
    public int getVersion() {
        return 1;
    }

    @Override
    @WebMethod(exclude = true)
    public String getServiceName() {
        return "postnummer";
    }

    @Override
    protected Class<DemoEntityRecord> getEntityClass() {
        return DemoEntityRecord.class;
    }


    @Override
    public Plugin getPlugin() {
        return this.demoPlugin;
    }

    @Override
    protected DemoRecordQuery getEmptyQuery() {
        return new DemoRecordQuery();
    }

    @Override
    protected void sendAsCSV(Stream<DemoEntityRecord> entities, HttpServletRequest request, HttpServletResponse response) throws IOException, HttpNotFoundException {

    }

    @Override
    protected void checkAccess(DafoUserDetails user)
        throws AccessDeniedException, AccessRequiredException {
        if (user == null) {
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
        query.applyFilters(session);
        Set<DemoEntity> entities = null;
        try {
            entities = new HashSet<>(QueryManager.getAllEntities(session, query, DemoEntity.class));
        } finally {
            session.close();
        }
        return entities;
    }

}
