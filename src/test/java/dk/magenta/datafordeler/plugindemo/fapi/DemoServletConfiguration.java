package dk.magenta.datafordeler.plugindemo.fapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.magenta.datafordeler.core.fapi.ServletConfiguration;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;

/**
 * Created by lars on 20-04-17.
 */
@Configuration
@EnableWs
public class DemoServletConfiguration extends ServletConfiguration {

    @Autowired
    private DemoPlugin demoPlugin;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private XmlMapper xmlMapper;

    @Override
    protected Plugin getPlugin() {
        return this.demoPlugin;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected XmlMapper getXmlMapper() {
        return this.xmlMapper;
    }

    @Override
    protected String getServiceOwner() {
        return "demo";
    }
}
