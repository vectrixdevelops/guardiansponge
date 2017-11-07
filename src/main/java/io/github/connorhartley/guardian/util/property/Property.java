package io.github.connorhartley.guardian.util.property;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
public @interface Property {

    String alias() default "";

    PropertyModifier modifier() default PropertyModifier.UNDEFINED;

}
