package dk.magenta.datafordeler.core.stereotypes;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used for marking expection of a DafoUser parameter in service methods.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DafoUser {}
