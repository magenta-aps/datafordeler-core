package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.fapi.ServiceDescriptor;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/")
public class IndexService {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SoapServiceConfiguration soapServiceConfiguration;

    // No need to recreate the response every time we are asked, since it's the same for every call on a given server execution
    private String preparedJsonResponse;

    private ArrayList<ServiceDescriptor> serviceDescriptors;

    @PostConstruct
    public void init() {
        this.pluginManager.addPostConstructCallBackHandler(new PluginManagerCallbackHandler() {

            @Override
            public void executePluginManagerCallback(PluginManager pluginManager) {
                HashMap<String, Pair<FapiService, Boolean>> serviceMap = new HashMap<>();

                for (Plugin plugin : pluginManager.getPlugins()) {
                    for (EntityManager entityManager : plugin.getRegisterManager().getEntityManagers()) {
                        FapiService restService = entityManager.getEntityService();
                        for (String servicePath : restService.getServicePaths()) {
                            serviceMap.put(servicePath, new ImmutablePair<>(restService, false));
                        }
                    }
                }

                for (JaxWsServerFactoryBean soapServiceBean : soapServiceConfiguration.getServerBeans()) {
                    FapiService soapService = (FapiService) soapServiceBean.getServiceBean();
                    serviceMap.put(soapServiceBean.getAddress(), new ImmutablePair<>(soapService, true));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode root = objectMapper.createObjectNode();

                IndexService.this.serviceDescriptors = new ArrayList<>();

                List<String> servicePaths = new ArrayList<>(serviceMap.keySet());
                Collections.sort(servicePaths);

                for (String servicePath : servicePaths) {
                    Pair<FapiService, Boolean> mapEntry = serviceMap.get(servicePath);
                    FapiService service = mapEntry.getLeft();
                    boolean isSoap = mapEntry.getRight();
                    ServiceDescriptor serviceDescriptor = service.getServiceDescriptor(servicePath, isSoap);
                    IndexService.this.serviceDescriptors.add(serviceDescriptor);
                }

                root.set("services", objectMapper.valueToTree(IndexService.this.serviceDescriptors));
                IndexService.this.preparedJsonResponse = root.toString();
            }
        });

    }

    @RequestMapping(path="", produces="application/json")
    public String json() {
        return this.preparedJsonResponse;
    }

    @RequestMapping(path="", produces="text/html")
    public ModelAndView html() {
        HashMap<String, Object> model = new HashMap<>();
        model.put("serviceDescriptors", this.serviceDescriptors);
        return new ModelAndView("serviceList", model);
    }

}
