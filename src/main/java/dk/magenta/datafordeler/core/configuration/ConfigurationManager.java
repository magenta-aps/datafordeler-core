package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.database.SessionManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;

/**
 * Created by lars on 06-04-17.
 * Plugin configurations are stored in separate database tables, each with only one
 * row. On loading the program, the Configuration is retrieved from the database, or
 * if one doesn’t exist (such as on the first run), created and saved.
 */
public abstract class ConfigurationManager<C extends Configuration> {

    private C configuration;

    /**
     * Locate Configuration object, or create one if none found.
     * Subclasses MUST call this in their initialization routines
     */
    public void init() {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Class<C> configurationClass = this.getConfigurationClass();
            this.configuration = session.createQuery("select c from "+configurationClass.getSimpleName()+" c", configurationClass).getSingleResult();
            this.getLog().info("Loaded configuration object");
        } catch (NoResultException e) {
            this.getLog().info("No configuration object exists, create one.");
            C defaultConfiguration = this.createConfiguration();
            session.save(defaultConfiguration);
            this.configuration = defaultConfiguration;
        }
        transaction.commit();
        session.close();
    }

    protected abstract Class<C> getConfigurationClass();

    protected abstract C createConfiguration();

    protected abstract SessionManager getSessionManager();

    public C getConfiguration() {
        return this.configuration;
    }

    protected abstract Logger getLog();
}
