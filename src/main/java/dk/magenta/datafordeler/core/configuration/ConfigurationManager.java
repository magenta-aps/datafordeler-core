package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.database.SessionManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.NoResultException;

/**
 * Created by lars on 06-04-17.
 */
public abstract class ConfigurationManager<C extends Configuration> {

    public void init() {
        C configuration = getConfiguration();
        if(configuration != null) {
            this.getLog().info("Loaded configuration object");
        } else {
            this.getLog().info("No configuration object exists, create one.");
            Session session = this.getSessionManager().getSessionFactory().openSession();
            configuration = this.createConfiguration();
            session.beginTransaction();
            session.persist(configuration);
            session.flush();
            session.close();
        }
    }

    protected abstract Class<C> getConfigurationClass();

    protected abstract C createConfiguration();

    protected abstract SessionManager getSessionManager();

    public C getConfiguration() {
        // This should always fetch fresh data from the database as that data might have been
        // changed.
        Session session = this.getSessionManager().getSessionFactory().openSession();
        C configuration;
        try {
            Class<C> configurationClass = this.getConfigurationClass();
            configuration = session.createQuery(
                "select c from " +configurationClass.getSimpleName() + " c",
                configurationClass
            ).getSingleResult();
            session.refresh(configuration);
        } catch (NoResultException e) {
            configuration = null;
        }
        session.close();
        return configuration;
    }

    protected abstract Logger getLog();
}
