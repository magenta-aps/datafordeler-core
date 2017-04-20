package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dk.magenta.datafordeler.core.exception.InvalidServiceOwnerDefinitionException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by lars on 20-04-17.
 */
public abstract class ServletConfiguration {

    private Logger log = LogManager.getLogger("FAPI Servlet configuration");

    protected abstract Plugin getPlugin();

    /**
     * Plugins must return the "owner" part of the FAPI services that will be hosted, e.g. "cpr", "cvr", "gis"
     * These will be placed in the servlet path of the FAPI: https://data.gl/[owner]/[service]/[version]/[method]
     * @return
     */
    protected abstract String getServiceOwner();

    private final Pattern ownerValidation = Pattern.compile("^[a-zA-Z0-9_]+$");

    private void validateServiceOwner(String serviceOwner) throws InvalidServiceOwnerDefinitionException {
        if (!ownerValidation.matcher(serviceOwner).matches()) {
            throw new InvalidServiceOwnerDefinitionException(this.getPlugin(), serviceOwner, this.ownerValidation);
        }
    }

    @Bean(name="pluginServletRegistration")
    public ServletRegistrationBean dispatcherServlet() throws InvalidServiceOwnerDefinitionException {

        ServletRegistrationBean servletRegistrationBean;

        CXFServlet cxfServlet = new CXFServlet();
        SpringBus bus = new SpringBus();
        cxfServlet.setBus(bus);

        RegisterManager registerManager = this.getPlugin().getRegisterManager();
        String serviceOwner = this.getServiceOwner();
        this.validateServiceOwner(serviceOwner);

        for (EntityManager entityManager : registerManager.getEntityManagers()) {
            FapiService service = entityManager.getEntityService();
            String base = "/" + service.getServiceName() + "/" + service.getVersion();

            // SOAP
            EndpointImpl soapEndpoint = new EndpointImpl(bus, service);
            soapEndpoint.publish(base + "/soap");
            soapEndpoint.setExecutor(Executors.newFixedThreadPool(10));
            this.log.info("Set up SOAP handler on " + base + "/soap");

            // REST
            JAXRSServerFactoryBean restEndpoint = new JAXRSServerFactoryBean();
            restEndpoint.setBus(bus);
            restEndpoint.setAddress(base + "/rest");
            restEndpoint.setServiceBean(service);
            ArrayList<Object> providers = new ArrayList<>();
            providers.add(new JacksonJaxbJsonProvider());
            restEndpoint.setProviders(providers);
            restEndpoint.create();
            this.log.info("Set up REST handler on " + base + "/rest");
        }

        servletRegistrationBean = new ServletRegistrationBean(cxfServlet, "/" + serviceOwner + "/*");
        servletRegistrationBean.setName(serviceOwner + "Servlet");

        return servletRegistrationBean;
    }
}
