package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import dk.magenta.datafordeler.core.exception.InvalidServiceOwnerDefinitionException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsServiceConfiguration;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.xml.ws.Provider;
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

    protected abstract ObjectMapper getObjectMapper();

    protected abstract XmlMapper getXmlMapper();

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

    /**
     * Sets up a servlet bean to listen for requests on defined paths
     * Plugins will be queried for EntityManagers, and for each one found, a SOAP and a REST interface
     * will be set up on the appropriate path (/<servicename>/<serviceversion>/soap and /<servicename>/<serviceversion>/soap)
     * Plugins must subclass FapiService and point to an instance with the EntityManager subclass method getEntityService()
     * @return A ServletRegistrationBean for use by Spring Boot
     * @throws InvalidServiceOwnerDefinitionException
     */
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
            JaxWsServerFactoryBean serverFactoryBean = new JaxWsServerFactoryBean();
            serverFactoryBean.setBus(bus);
            serverFactoryBean.setAddress(base + "/soap");
            serverFactoryBean.setServiceBean(service);
            serverFactoryBean.create();
            this.log.info("Set up SOAP handler on " + base + "/soap");

            // REST
            JAXRSServerFactoryBean restEndpoint = new JAXRSServerFactoryBean();
            restEndpoint.setBus(bus);
            restEndpoint.setAddress(base + "/rest");
            restEndpoint.setServiceBean(service);
            restEndpoint.setProvider(new JacksonJaxbJsonProvider(this.getObjectMapper(), new Annotations[]{Annotations.JACKSON}));
            restEndpoint.setProvider(new JacksonJaxbXMLProvider(this.getXmlMapper(), new Annotations[]{Annotations.JACKSON}));
            restEndpoint.create();
            this.log.info("Set up REST handler on " + base + "/rest");
        }

        servletRegistrationBean = new ServletRegistrationBean(cxfServlet, "/" + serviceOwner + "/*");
        servletRegistrationBean.setName(serviceOwner + "Servlet");

        return servletRegistrationBean;
    }
}
