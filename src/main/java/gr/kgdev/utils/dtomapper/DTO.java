package gr.kgdev.utils.dtomapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DTO {
	String value() default "";
}
