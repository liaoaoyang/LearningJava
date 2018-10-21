package co.iay.learn.learningjava.basic;

import java.lang.annotation.*;

@Repeatable(LearnRepeatableAnnotation.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
public @interface LearnAnnotation {
    String value() default "";

    String filedAnnotationValue() default "";

    Class<?> className() default Void.class;
}
