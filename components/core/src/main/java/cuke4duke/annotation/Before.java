package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CucumberAnnotation("en")
public @interface Before {
    String[] value() default "";
}