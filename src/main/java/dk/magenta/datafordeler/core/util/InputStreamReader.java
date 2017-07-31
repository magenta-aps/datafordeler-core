package dk.magenta.datafordeler.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class InputStreamReader {

    private static final String STREAM_DELIMITER = "\\A";

    public static String readInputStream(InputStream stream) {
        String data = new Scanner(stream).useDelimiter(STREAM_DELIMITER).next();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
