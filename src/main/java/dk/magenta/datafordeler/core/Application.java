package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.InvalidServiceOwnerDefinitionException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.fapi.SoapHandler;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by lars on 12-01-17.
 */
@ComponentScan({"dk.magenta.datafordeler", "dk.magenta.datafordeler.core", "dk.magenta.datafordeler.core.gapi", "dk.magenta.datafordeler.core.database", "dk.magenta.datafordeler.core.util"})
@EntityScan("dk.magenta.datafordeler")
@ServletComponentScan
@SpringBootApplication
@EnableScheduling
public class Application {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    XmlMapper xmlMapper;

    @Autowired
    SessionManager sessionManager;




    public static final int servicePort = 8445;

    public static void main(final String[] args) throws Exception {

        extendClassLoader(Thread.currentThread().getContextClassLoader());

        try {
            SpringApplication.run(Application.class, args);
        } catch (Throwable e) {
            while (e != null) {
                if (e instanceof com.sun.xml.bind.v2.runtime.IllegalAnnotationsException) {
                    System.out.println(((com.sun.xml.bind.v2.runtime.IllegalAnnotationsException) e).getErrors());
                }
                e = e.getCause();
            }
        }
    }

    /* For testing the servlet in a throwaway Tomcat container */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        return new TomcatEmbeddedServletContainerFactory(servicePort);
    }

    private static Logger log = LogManager.getLogger(Application.class);

    private final Pattern ownerValidation = Pattern.compile("^[a-zA-Z0-9_]+$");


    @Bean
    protected ServletRegistrationBean fapiDispatcherServlet()
        throws InvalidServiceOwnerDefinitionException {
        // this.log.info("Initialize ServletRegistrationBean for Plugin "+this.getPlugin().getClass().getCanonicalName());

        RootMatchingCXFServlet cxfServlet = new RootMatchingCXFServlet();
        SpringBus bus = new SpringBus();
        //bus.setExtension(classLoader, ClassLoader.class);
        cxfServlet.setBus(bus);


        this.log.info("Known plugins;");
        for (Plugin plugin : pluginManager.getPlugins()) {
            this.log.info(plugin.getName());
        }

        List<String> serviceOwnerMatchers = new ArrayList<>();
        //Thread.currentThread().setContextClassLoader(classLoader);

        for (Plugin plugin : pluginManager.getPlugins()) {
            RegisterManager registerManager = plugin.getRegisterManager();
            this.log.info("Handling plugin "+plugin.getName()+" with "+registerManager.getEntityManagers().size()+" EntityManagers");
            String serviceOwner = plugin.getServiceOwner();
            this.log.info("Service owner for " + plugin.getClass().getCanonicalName() + " is " + serviceOwner);
            if (!ownerValidation.matcher(serviceOwner).matches()) {
                this.log.error("Invalid service owner: " + serviceOwner);
                throw new InvalidServiceOwnerDefinitionException(plugin, serviceOwner, this.ownerValidation);
            }

            for (EntityManager entityManager : registerManager.getEntityManagers()) {
                FapiService service = entityManager.getEntityService();
                String base = "/" + serviceOwner + "/" + service.getServiceName() + "/" + service.getVersion();

                // SOAP
                this.log.info("Setting up SOAP handler on " + base + "/soap");
                JaxWsServerFactoryBean serverFactoryBean = new JaxWsServerFactoryBean();

                serverFactoryBean.setBus(bus);
                serverFactoryBean.setAddress(base + "/soap");
                serviceOwnerMatchers.add(base + "/soap");
                serverFactoryBean.setServiceBean(service);
                serverFactoryBean.addHandlers(Collections.singletonList(new SoapHandler()));
                serverFactoryBean.create();
            }
        }

        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(
            cxfServlet,
            //"/*"
            serviceOwnerMatchers.toArray(new String[serviceOwnerMatchers.size()])
        );
        servletRegistrationBean.setName("FapiServlet");
        this.log.info("ServletRegistrationBean \"" + servletRegistrationBean.getServletName() + "\" created");

        return servletRegistrationBean;
    }

    // Seriously unholy reflection magic, to force our URLs into the existing classloader
    private static void extendClassLoader(ClassLoader classLoader) {
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            File pluginFolder = new File("../plugins/jar/");
            File[] files = pluginFolder.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null) {
                for (File file : files) {
                    try {
                        method.invoke(urlClassLoader, file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        log.error("Invalid URL for Jar file", e);
                    }
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e);
        }
    }

}