package dk.magenta.datafordeler.core.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Created by jubk on 21-06-2017.
 */
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
  public UserQueryManager userQueryManager() {
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
  public DataSource userDataSource() {
    DataSourceProperties dataSourceProperties = new DataSourceProperties();
    dataSourceProperties.setUrl(url);
    dataSourceProperties.setUsername(username);
    dataSourceProperties.setPassword(password);
    dataSourceProperties.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    return dataSourceProperties.initializeDataSourceBuilder().build();
  }

  /**
   * Builds a JdbcTemplate bean used for working with the DAFO users database.
   * @return A configured JdbcTemplate
   */
  public JdbcTemplate userJdbcTemplate() {
    DataSource dataSource = userDataSource();
    return new JdbcTemplate(dataSource);
  }
}
