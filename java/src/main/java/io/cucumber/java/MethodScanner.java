package io.cucumber.java;

import io.cucumber.java.api.After;
import io.cucumber.java.api.AfterStep;
import io.cucumber.java.api.Before;
import io.cucumber.java.api.BeforeStep;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static io.cucumber.core.io.MultiLoader.packageName;

class MethodScanner {

    private final ClassFinder classFinder;

    MethodScanner(ClassFinder classFinder) {
        this.classFinder = classFinder;
    }

    /**
     * Registers step definitions and hooks.
     *
     * @param javaBackend the backend where stepdefs and hooks will be registered
     * @param gluePaths   where to look
     */
    void scan(JavaBackend javaBackend, List<String> gluePaths) {
        for (String gluePath : gluePaths) {
            for (Class<?> glueCodeClass : classFinder.getDescendants(Object.class, packageName(gluePath))) {
                while (glueCodeClass != null && glueCodeClass != Object.class && !Reflections.isInstantiable(glueCodeClass)) {
                    // those can't be instantiated without container class present.
                    glueCodeClass = glueCodeClass.getSuperclass();
                }
                //prevent unnecessary checking of Object methods
                if (glueCodeClass != null && glueCodeClass != Object.class) {
                    for (Method method : glueCodeClass.getMethods()) {
                        if (method.getDeclaringClass() != Object.class) {
                            scan(javaBackend, method, glueCodeClass);
                        }
                    }
                }
            }
        }
    }

    /**
     * Registers step definitions and hooks.
     *
     * @param javaBackend   the backend where stepdefs and hooks will be registered.
     * @param method        a candidate for being a stepdef or hook.
     * @param glueCodeClass the class where the method is declared.
     */
    void scan(JavaBackend javaBackend, Method method, Class<?> glueCodeClass) {
        Annotation[] methodAnnotations = method.getAnnotations();
        for (Annotation annotation : methodAnnotations) {
            if (isHookAnnotation(annotation)) {
                validateMethod(method, glueCodeClass);
                javaBackend.addHook(annotation, method);
            } else if (isStepdefAnnotation(annotation)) {
                validateMethod(method, glueCodeClass);
                javaBackend.addStepDefinition(annotation, method);
            }
        }
    }

    private void validateMethod(Method method, Class<?> glueCodeClass) {
        if (!method.getDeclaringClass().isAssignableFrom(glueCodeClass)) {
            throw new CucumberException(String.format("%s isn't assignable from %s", method.getDeclaringClass(), glueCodeClass));
        }
        if (!glueCodeClass.equals(method.getDeclaringClass())) {
            throw new CucumberException(String.format("You're not allowed to extend classes that define Step Definitions or hooks. %s extends %s", glueCodeClass, method.getDeclaringClass()));
        }
    }

    private boolean isHookAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.equals(Before.class) || annotationClass.equals(After.class) || annotationClass.equals(BeforeStep.class) || annotationClass.equals(AfterStep.class);
    }

    private boolean isStepdefAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.getAnnotation(StepDefAnnotation.class) != null;
    }
}
