package dk.magenta.datafordeler.core.database;

import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
public class SessionManager {

    private SessionFactory sessionFactory;

    private Logger log = LogManager.getLogger("SessionManager");

    public SessionManager(SessionManagerConfiguration smConfig) {
        try {
            this.log.info("Initialize SessionManager");
            // Create the SessionFactory from hibernate.cfg.xml

            // Create empty configuration object
            Configuration configuration = new Configuration();

            this.log.info("Loading configuration from hibernate.cfg.xml");
            Properties props = System.getProperties();
            configuration.configure(smConfig.getHibernateConfigurationFile());

            Set<Class> managedClasses = new HashSet<>();
            managedClasses.add(dk.magenta.datafordeler.core.database.Identification.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Entity.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Registration.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Effect.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.DataItem.class);

            for (Class cls : managedClasses) {
                this.log.info("Located hardcoded data class "+cls.getCanonicalName());
            }

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));

            Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName());
                this.log.info("Located autodetected data class "+cls.getCanonicalName());
                managedClasses.add(cls);
            }
            for (Class cls : managedClasses) {
                this.log.info("Adding managed data class "+cls.getCanonicalName());
                configuration.addAnnotatedClass(cls);
            }

            // Create our session factory
            this.log.info("Creating SessionFactory");
            this.sessionFactory = configuration.buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            this.log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    @PreDestroy
    public void shutdown() {
        this.log.info("Shutting down SessionManager. Closing SessionFactory.");
        // Close caches and connection pools
        this.sessionFactory.close();
    }

}
