package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.DumpInfo;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.exception.AccessRequiredException;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.role.ReadServiceRole;
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
    private DafoUserManager dafoUserManager;

    private Logger log = LoggerFactory.getLogger("IndexDumpService");

    @PostConstruct
    public void init() {
    }

    protected void checkAccess(DafoUserDetails dafoUserDetails, Plugin plugin)
        throws AccessDeniedException {
        ReadServiceRole pluginDefaultReadRole = plugin.getRolesDefinition().getDefaultReadRole();
        dafoUserDetails.checkHasSystemRole(pluginDefaultReadRole);
    }

    private boolean hasAccess(DafoUserDetails dafoUserDetails, Plugin plugin) {
        try {
            this.checkAccess(dafoUserDetails, plugin);
            return true;
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    protected void checkAndLogAccess(LoggerHelper loggerHelper, String pluginName)
        throws AccessDeniedException, AccessRequiredException {
        try {
            Plugin plugin = pluginManager.getPluginByName(pluginName);
            this.checkAccess(loggerHelper.getUser(), plugin);
        }
        catch(AccessDeniedException e) {
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
            if(hasAccess(user, plugin))
                filter.put("plugin", plugin.getName());
        }

        loggerHelper.info("Requesting dump list with following filter: " + filter.toString());

        if(filter.isEmpty())
            throw new AccessRequiredException(
                "You need to have access to read from at least one plugin"
            );

        HashMap<String, Object> model = new HashMap<>();
        Session session = sessionManager.getSessionFactory().openSession();
        List<DumpInfo> resultList = QueryManager.getItems(session, DumpInfo.class, filter);
        model.put("dumpList", resultList);
        return new ModelAndView("dumpList", model);
    }

    @RequestMapping(path="json", produces="application/json")
    public String json(@RequestParam("plugin") String plugin, @RequestParam("id") Long id,
        HttpServletRequest request)
        throws InvalidTokenException, AccessDeniedException, AccessRequiredException {
        return getDumpData(request, id, plugin, "json");
    }

    @RequestMapping(path="xml", produces="application/xml")
    public String xml(@RequestParam("plugin") String plugin, @RequestParam("id") Long id,
        HttpServletRequest request)
        throws InvalidTokenException, AccessDeniedException, AccessRequiredException {
        return getDumpData(request, id, plugin, "xml");
    }

    @RequestMapping(path="csv", produces="text/csv")
    public String csv(@RequestParam("plugin") String plugin, @RequestParam("id") Long id,
        HttpServletRequest request)
        throws InvalidTokenException, AccessDeniedException, AccessRequiredException {
        return getDumpData(request, id, plugin, "csv");
    }

    private String getDumpData(HttpServletRequest request, Long id, String plugin, String format)
        throws InvalidTokenException, AccessDeniedException, AccessRequiredException {

        DafoUserDetails user = dafoUserManager.getUserFromRequest(request);
        LoggerHelper loggerHelper = new LoggerHelper(log, request, user);

        loggerHelper.info("Requesting dump from plugin: " + plugin
            + " for entity with id: " + id);

        this.checkAndLogAccess(loggerHelper, plugin);

        Session session = sessionManager.getSessionFactory().openSession();
        Map<String, Object> filter = new HashMap<>();
        filter.put("id", id);
        filter.put("plugin", plugin);
        filter.put("format", format);
        DumpInfo result = QueryManager.getItem(session, DumpInfo.class, filter);

        return result.getData();
    }
}
