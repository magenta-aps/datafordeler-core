package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.model.Entity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by lars on 21-02-17.
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

            configuration.addAnnotatedClass(dk.magenta.datafordeler.core.model.Identification.class);
            configuration.addAnnotatedClass(dk.magenta.datafordeler.core.model.Entity.class);
            configuration.addAnnotatedClass(dk.magenta.datafordeler.core.model.Registration.class);
            configuration.addAnnotatedClass(dk.magenta.datafordeler.core.model.Effect.class);
            configuration.addAnnotatedClass(dk.magenta.datafordeler.core.model.DataItem.class);

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));
            //componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.MappedSuperclass.class));

            Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName());
                System.out.println("CLASS: "+cls.getName());
                configuration.addAnnotatedClass(cls);
            }

            // Create our session factory
            this.sessionFactory = configuration.buildSessionFactory();

//            LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);
//            sessionBuilder.scanPackages("dk.magenta.datafordeler");
//            this.sessionFactory = sessionBuilder.buildSessionFactory();
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
