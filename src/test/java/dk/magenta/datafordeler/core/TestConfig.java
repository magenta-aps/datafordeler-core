package dk.magenta.datafordeler.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lars on 12-01-17.
 */
@Configuration
@ComponentScan(basePackages = "dk.magenta.datafordeler")
@ServletComponentScan
@SpringBootApplication
public class TestConfig {

    public static final int servicePort = 8443;

    /* For testing the servlet in a throwaway Tomcat container */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        return new TomcatEmbeddedServletContainerFactory(servicePort);
    }
}
