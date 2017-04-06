package dk.magenta.datafordeler.core.configuration;

import org.hibernate.Session;

import javax.persistence.NoResultException;

/**
 * Created by lars on 06-04-17.
 */
public abstract class ConfigurationManager<C extends Configuration> {

    public void init(Session session) {
        this.createDefaultIfMissing(session);
    }

    protected abstract Class<C> getConfigurationClass();

    public C getConfiguration(Session session) {
        try {
            Class<C> configurationClass = this.getConfigurationClass();
            return session.createQuery("select c from "+configurationClass.getSimpleName()+" c", configurationClass).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    protected abstract C initConfiguration();

    protected C createDefaultIfMissing(Session session) {
        if (this.getConfiguration(session) == null) {
            C defaultConfiguration = this.initConfiguration();
            session.save(defaultConfiguration);
            return defaultConfiguration;
        }
        return null;
    }
}
