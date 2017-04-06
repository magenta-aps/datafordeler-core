package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.SessionManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;

/**
 * Created by lars on 06-04-17.
 */
public abstract class ConfigurationManager<C extends Configuration> {

    private C configuration;

    public void init() {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            Class<C> configurationClass = this.getConfigurationClass();
            this.configuration = session.createQuery("select c from "+configurationClass.getSimpleName()+" c", configurationClass).getSingleResult();
        } catch (NoResultException e) {
            C defaultConfiguration = this.initConfiguration();
            session.save(defaultConfiguration);
            this.configuration = defaultConfiguration;
        }
        session.close();
    }

    protected abstract Class<C> getConfigurationClass();

    protected abstract C initConfiguration();

    protected abstract SessionManager getSessionManager();

}
