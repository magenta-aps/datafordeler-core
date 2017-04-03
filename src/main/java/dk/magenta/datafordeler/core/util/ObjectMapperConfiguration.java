package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Created by lars on 24-02-17.
 */
@Configuration
public class ObjectMapperConfiguration {

    /**
     * ObjectMapper configuration; add serializers here
     */
    @Primary
    @Bean
    public ObjectMapper objectMapper() {

        System.out.println("ObjectMapper bean called;");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime));
            }
        });
        dateModule.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(tokenValue));
                } else {
                    return null;
                }
            }
        });
        objectMapper.registerModule(dateModule);

        SimpleModule uuidModule = new SimpleModule();
        uuidModule.addSerializer(UUID.class, new JsonSerializer<UUID>() {
            @Override
            public void serialize(UUID uuid, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(uuid.toString());
            }
        });
        uuidModule.addDeserializer(UUID.class, new JsonDeserializer<UUID>() {
            @Override
            public UUID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return UUID.fromString(tokenValue);
                } else {
                    return null;
                }
            }
        });
        objectMapper.registerModule(uuidModule);

        return objectMapper;
    }

}
