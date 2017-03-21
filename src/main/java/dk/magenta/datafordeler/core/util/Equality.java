package dk.magenta.datafordeler.core.util;

/**
 * Created by lars on 21-03-17.
 */
public abstract class Equality {

    public static boolean equal(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

}
