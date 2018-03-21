package dk.magenta.datafordeler.core.user;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.beans.PropertyVetoException;

@Component
public class UserDatabaseConfig {

  @Value("${dafo.userdatabase.enabled:false}")
  private boolean enabled;
  @Value("${dafo.userdatabase.url:}")
  private String url;
  @Value("${dafo.userdatabase.username:}")
  private String username;
  @Value("${dafo.userdatabase.password:}")
  private String password;

  @Bean
  public UserQueryManager userQueryManager() throws ConfigurationException {
    if (enabled) {
      return new UserQueryManagerImpl(userJdbcTemplate());
    } else {
      return new NoDBUserQueryManager();
    }
  }

  /**
   * Generates a datasource to use for creating the userJdbcTemplate bean.
   * This method can _not_ create a bean as spring boot autoconfiguration will try to use that
   * bean for the primary DAFO database and fail.
   * This also means that it is not possible to configuration property injection to configure
   * the datasource.
   * @return A configured DataSource
   */
  public ComboPooledDataSource userDataSource() throws ConfigurationException {
    ComboPooledDataSource pooledDataSource = new ComboPooledDataSource();
    try {
      pooledDataSource.setJdbcUrl(url);
      pooledDataSource.setUser(username);
      pooledDataSource.setPassword(password);
      pooledDataSource.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      pooledDataSource.setMinPoolSize(5);
      pooledDataSource.setMaxPoolSize(200);
      pooledDataSource.setMaxStatements(50);
      pooledDataSource.setIdleConnectionTestPeriod(3000);
    } catch (PropertyVetoException e) {
        throw new ConfigurationException(e);
    }
    return pooledDataSource;
  }

  /**
   * Builds a JdbcTemplate bean used for working with the DAFO users database.
   * @return A configured JdbcTemplate
   */
  public JdbcTemplate userJdbcTemplate() throws ConfigurationException {
    return new JdbcTemplate(this.userDataSource());
  }
}
