package web_project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})        // куда можно вешать: METHOD — на метод
@Retention(RetentionPolicy.RUNTIME) // чтобы работало в рантайме (нужно для AOP)
public @interface Loggable {
    String value() default "";
}
