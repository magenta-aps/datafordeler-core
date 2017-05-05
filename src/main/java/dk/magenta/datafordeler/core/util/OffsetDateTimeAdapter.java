package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by lars on 04-05-17.
 */
public class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {
    @Override
    public String marshal(OffsetDateTime offsetDateTime) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime);
    }

    @Override
    public OffsetDateTime unmarshal(String stringRepresentation) {
        return OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(stringRepresentation));
    }

    public StdSerializer<OffsetDateTime> getSerializer() {
        return new StdSerializer<OffsetDateTime>(OffsetDateTime.class) {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator
                    jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(OffsetDateTimeAdapter.this.marshal(offsetDateTime));

            }
        };
    }

    public StdDeserializer<OffsetDateTime> getDeserializer() {
        return new StdDeserializer<OffsetDateTime>(OffsetDateTime.class) {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                    jsonParser.nextToken();
                }
                String tokenValue = jsonParser.getValueAsString();
                if (tokenValue != null) {
                    return OffsetDateTimeAdapter.this.unmarshal(tokenValue);
                } else {
                    return null;
                }
            }
        };
    }
}
