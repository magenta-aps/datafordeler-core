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
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import java.util.regex.Pattern;

/**
 * Created by lars on 20-04-17.
 */
public abstract class ServletConfiguration {

    private Logger log = LogManager.getLogger("FAPI ServletConfiguration");

    protected abstract Plugin getPlugin();

    protected abstract ObjectMapper getObjectMapper();

    protected abstract XmlMapper getXmlMapper();

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
        this.log.info("Initialize ServletRegistrationBean for Plugin "+this.getPlugin().getClass().getCanonicalName());
        ServletRegistrationBean servletRegistrationBean;

        CXFServlet cxfServlet = new CXFServlet();
        this.log.info("Setting up Spring Bus");
        SpringBus bus = new SpringBus();
        cxfServlet.setBus(bus);

        RegisterManager registerManager = this.getPlugin().getRegisterManager();
        String serviceOwner = this.getServiceOwner();
        this.log.info("Service owner for " + this.getPlugin().getClass().getCanonicalName() + " is " + serviceOwner);
        this.validateServiceOwner(serviceOwner);

        for (EntityManager entityManager : registerManager.getEntityManagers()) {
            FapiService service = entityManager.getEntityService();
            String base = "/" + service.getServiceName() + "/" + service.getVersion();

            // SOAP
            this.log.info("Setting up SOAP handler on " + base + "/soap");
            JaxWsServerFactoryBean serverFactoryBean = new JaxWsServerFactoryBean();
            serverFactoryBean.setBus(bus);
            serverFactoryBean.setAddress(base + "/soap");
            serverFactoryBean.setServiceBean(service);
            serverFactoryBean.create();

            // REST
            this.log.info("Setting up REST handler on " + base + "/rest");
            JAXRSServerFactoryBean restEndpoint = new JAXRSServerFactoryBean();
            restEndpoint.setBus(bus);
            restEndpoint.setAddress(base + "/rest");
            restEndpoint.setServiceBean(service);
            restEndpoint.setProvider(new JacksonJaxbJsonProvider(this.getObjectMapper(), new Annotations[]{Annotations.JACKSON}));
            restEndpoint.setProvider(new JacksonJaxbXMLProvider(this.getXmlMapper(), new Annotations[]{Annotations.JACKSON}));
            restEndpoint.create();
        }

        servletRegistrationBean = new ServletRegistrationBean(cxfServlet, "/" + serviceOwner + "/*");
        servletRegistrationBean.setName(serviceOwner + "Servlet");
        this.log.info("ServletRegistrationBean \"" + servletRegistrationBean.getServletName() + "\" created");

        return servletRegistrationBean;
    }


    /**
     * Plugins must return the "owner" part of the FAPI services that will be hosted, e.g. "cpr", "cvr", "gis"
     * These will be placed in the servlet path of the FAPI: https://data.gl/[owner]/[service]/[version]/[method]
     * @return
     */
    protected abstract String getServiceOwner();

    private final Pattern ownerValidation = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * Validates that the given serviceOwner matches the allowed pattern
     * @param serviceOwner
     * @throws InvalidServiceOwnerDefinitionException
     */
    private void validateServiceOwner(String serviceOwner) throws InvalidServiceOwnerDefinitionException {
        if (!ownerValidation.matcher(serviceOwner).matches()) {
            this.log.error("Invalid service owner: " + serviceOwner);
            throw new InvalidServiceOwnerDefinitionException(this.getPlugin(), serviceOwner, this.ownerValidation);
        }
    }
}
