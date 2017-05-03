package dk.magenta.datafordeler.core.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class SessionManager {

    private SessionFactory sessionFactory;

    public SessionManager() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml

            // Create empty configuration object
            Configuration configuration = new Configuration();
            // Populate it from hibernate.cfg.xml
            configuration.configure();

            Set<Class> managedClasses = new HashSet<>();
            managedClasses.add(dk.magenta.datafordeler.core.database.Identification.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Entity.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Registration.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Effect.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.DataItem.class);

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));

            Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName());
                managedClasses.add(cls);
            }
            for (Class cls : managedClasses) {
                configuration.addAnnotatedClass(cls);
            }

            // Create our session factory
            this.sessionFactory = configuration.buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    @PreDestroy
    public void shutdown() {
        // Close caches and connection pools
        this.sessionFactory.close();
    }

    public String getTableName(Entity cls) {
        Table table = Entity.class.getAnnotation(Table.class);
        return table.name();
    }

}
