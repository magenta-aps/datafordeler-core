package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Controller
@RequestMapping(path="/monitor")
public class MonitorService {

    @Autowired
    SessionManager sessionManager;

    @RequestMapping(path="/database")
    public void checkMonitoring(HttpServletRequest request, HttpServletResponse response) {
        Session session = sessionManager.getSessionFactory().openSession();
        Query query = session.createQuery("select 1 from Identification").setMaxResults(1);
        query.uniqueResult();
        session.close();
        response.setStatus(200);
    }
}
