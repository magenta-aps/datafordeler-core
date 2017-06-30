package dk.magenta.datafordeler.core.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by jubk on 30-06-2017.
 */
@Component
public class SessionManagerConfiguration {

  @Value("${dafo.hibernate.configuration-file:/hibernate.cfg.xml}")
  private String hibernateConfigurationFile;

  public String getHibernateConfigurationFile() {
    return hibernateConfigurationFile;
  }

  @Bean
  public SessionManager sessionManager(SessionManagerConfiguration configuration) {
    return new SessionManager(configuration);
  }
}
