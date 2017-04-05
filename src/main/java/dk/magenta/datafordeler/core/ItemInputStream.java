package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

/**
 * Created by lars on 16-02-17.
 */
public class ItemInputStream<T> extends ObjectInputStream {

    public ItemInputStream(InputStream in) throws IOException {
        super(in);
    }

    public T next() throws IOException {
        try {
            // Return a Checksum object from the stream; only Checksum objects
            // should be present there
            return (T) this.readObject();
        } catch (ClassNotFoundException e) {
            // The next object in the stream is of a class we can't find
            // This would mean that there's a non-Checksum object in there.
            // Ignore it and get the next one instead
            return this.next();
        }
    }

    public static <T> ItemInputStream<T> parseJsonStream(InputStream jsonStream, Class<T> itemClass, ObjectMapper objectMapper) {
        PipedInputStream inputStream = new PipedInputStream();
        try {
            final PipedOutputStream outputStream = new PipedOutputStream(inputStream);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            final String objectListName = "items";

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JsonParser jsonParser = new JsonFactory().createParser(jsonStream);
                        JsonToken previousToken = null;

                        int level = 0;
                        int listItemLevel = -1;
                        for (JsonToken nextToken = jsonParser.nextToken(); nextToken != null && level < 1000; nextToken = jsonParser.nextToken()) {
                            if (nextToken == JsonToken.START_OBJECT) {
                                level++;
                                if (level == listItemLevel) {
                                    T item = objectMapper.readValue(jsonParser, itemClass);
                                    objectOutputStream.writeObject(item);
                                    level--;
                                }
                            } else if (nextToken == JsonToken.END_OBJECT) {
                                level--;

                            } else if (nextToken == JsonToken.START_ARRAY && previousToken == JsonToken.FIELD_NAME && objectListName.equals(jsonParser.getCurrentName())) {
                                listItemLevel = level + 1;
                            }
                            if (level == 0) {
                                break;
                            }
                            previousToken = nextToken;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            objectOutputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            });
            t.start();

            return new ItemInputStream<T>(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
