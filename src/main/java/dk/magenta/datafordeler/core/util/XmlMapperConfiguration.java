package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by lars on 24-02-17.
 */
@Configuration
public class XmlMapperConfiguration {

    /**
     * Creates a module to serialize and deserialize objects of type "java.time.OffsetDateTime"
     * @return The created module
     */
    private SimpleModule getOffsetDateTimeModule() {
        SimpleModule dateModule = new SimpleModule();
        OffsetDateTimeAdapter adapter = new OffsetDateTimeAdapter();
        dateModule.addSerializer(adapter.getSerializer());
        dateModule.addDeserializer(OffsetDateTime.class, adapter.getDeserializer());
        return dateModule;
    }

    /**
     * XmlMapper configuration; add serializers here
     */
    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper(new JacksonXmlModule());
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.registerModule(this.getOffsetDateTimeModule());
        return xmlMapper;
    }

}
