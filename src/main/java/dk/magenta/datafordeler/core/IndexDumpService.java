package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.DumpData;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.LoggerHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@ResponseBody
@RequestMapping("dump")
public class IndexDumpService {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private QueryManager queryManager;

    @Autowired
    private DafoUserManager dafoUserManager;

    private Logger log = LoggerFactory.getLogger("IndexDumpService");

    @PostConstruct
    public void init() {
    }

    protected void checkAccess(DafoUserDetails dafoUserDetails, String pluginName) throws
        AccessDeniedException, AccessRequiredException {

        Plugin plugin = pluginManager.getPluginByName(pluginName);

        //dafoUserDetails.checkHasSystemRole(plugin.getDefaultReadRole());
    }

    private boolean hasAccess(DafoUserDetails dafoUserDetails, String pluginName) {
        try {
            this.checkAccess(dafoUserDetails, pluginName);
            return true;
        } catch (AccessDeniedException | AccessRequiredException e) {
            return false;
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper, String pluginName)
        throws AccessDeniedException, AccessRequiredException {
        try {
            this.checkAccess(loggerHelper.getUser(), pluginName);
        }
        catch(AccessDeniedException|AccessRequiredException e) {
            loggerHelper.info("Access denied: " + e.getMessage());
            throw(e);
        }
    }

    @RequestMapping(path="list", produces="text/html")
    public ModelAndView html(HttpServletRequest request)
        throws InvalidTokenException, AccessRequiredException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);

        Map<String, Object> filter = new HashMap<>();
        for (Plugin plugin : pluginManager.getPlugins()) {
            String pluginName = plugin.getName();
            if(hasAccess(user, pluginName))
                filter.put("plugin", pluginName);
        }

        loggerHelper.info("Requesting dump list with following filter: " + filter.toString());

        if(filter.isEmpty())
            throw new AccessRequiredException(
                "You need to have access to read from at least one plugin"
            );

        HashMap<String, Object> model = new HashMap<>();
        Session session = sessionManager.getSessionFactory().openSession();
        List<DumpData> resultList = queryManager.getItems(session, DumpData.class, filter);
        model.put("dumpList", resultList);
        return new ModelAndView("dumpList", model);
    }

    @RequestMapping(path="", produces="application/json")
    public String json(@RequestParam("plugin") String plugin, @RequestParam("id") Long id, HttpServletRequest request)
        throws InvalidTokenException, AccessDeniedException, AccessRequiredException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);

        loggerHelper.info("Requesting dump from plugin: " + plugin
            + " for entity with id: " + id);

        this.checkAndLogAccess(loggerHelper, plugin);

        Session session = sessionManager.getSessionFactory().openSession();
        Map<String, Object> filter = new HashMap<>();
        filter.put("id", id);
        DumpData result = queryManager.getItem(session, DumpData.class, filter);

        return result.getData();
    }
}
