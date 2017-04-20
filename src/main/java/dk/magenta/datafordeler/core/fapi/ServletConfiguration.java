package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.exception.InvalidServiceOwnerDefinitionException;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by lars on 20-04-17.
 */
public abstract class ServletConfiguration {

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

            // REST
            JAXRSServerFactoryBean restEndpoint = new JAXRSServerFactoryBean();
            restEndpoint.setBus(bus);
            restEndpoint.setAddress(base + "/rest");
            restEndpoint.setServiceBeans(Collections.singletonList(service));
        }

        servletRegistrationBean = new ServletRegistrationBean(cxfServlet, "/" + serviceOwner + "/*");
        servletRegistrationBean.setName(serviceOwner + "Servlet");

        return servletRegistrationBean;
    }
}
