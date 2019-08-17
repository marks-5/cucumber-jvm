package io.cucumber.testng;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.snippets.SnippetType;

final class TestNGCucumberOptionsProvider implements CucumberOptionsAnnotationParser.OptionsProvider {
    @Override
    public CucumberOptionsAnnotationParser.CucumberOptions getOptions(Class<?> clazz) {
        CucumberOptions annotation = clazz.getAnnotation(CucumberOptions.class);
        if (annotation == null) {
            return null;
        }
        return new TestNGCucumberOptions(annotation);
    }

    private static class TestNGCucumberOptions implements CucumberOptionsAnnotationParser.CucumberOptions {
        private final CucumberOptions annotation;

        TestNGCucumberOptions(CucumberOptions annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean dryRun() {
            return annotation.dryRun();
        }

        @Override
        public boolean strict() {
            return annotation.strict();
        }

        @Override
        public String[] features() {
            return annotation.features();
        }

        @Override
        public String[] glue() {
            return annotation.glue();
        }

        @Override
        public String[] extraGlue() {
            return annotation.extraGlue();
        }

        @Override
        public String[] tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
        }

        @Override
        public boolean monochrome() {
            return annotation.monochrome();
        }

        @Override
        public String[] name() {
            return annotation.name();
        }

        @Override
        public SnippetType snippets() {
            switch (annotation.snippets()) {
                case UNDERSCORE:
                    return SnippetType.UNDERSCORE;
                case CAMELCASE:
                    return SnippetType.CAMELCASE;
                default:
                    throw new IllegalArgumentException("" + annotation.snippets());
            }
        }

        @Override
        public Class<? extends ObjectFactory> objectFactory() {
            return (annotation.objectFactory() == NoObjectFactory.class) ? null : annotation.objectFactory();
        }
    }
}
