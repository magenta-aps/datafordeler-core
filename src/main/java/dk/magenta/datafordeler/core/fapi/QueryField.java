package dk.magenta.datafordeler.core.fapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lars on 23-05-17.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {
    public enum FieldType {
        STRING, INT, BOOLEAN
    }
    FieldType type() default FieldType.STRING;
}
