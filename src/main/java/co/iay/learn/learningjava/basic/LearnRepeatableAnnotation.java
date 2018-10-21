package co.iay.learn.learningjava.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
public @interface LearnRepeatableAnnotation {
    LearnAnnotation[] value() default {};
}
