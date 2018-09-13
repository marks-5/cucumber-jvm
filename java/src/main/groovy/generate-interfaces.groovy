import gherkin.GherkinDialectProvider
import groovy.text.SimpleTemplateEngine

SimpleTemplateEngine engine = new SimpleTemplateEngine()

def unsupported = ["em"] // The generated files for Emoij do not compile.
def dialectProvider = new GherkinDialectProvider()

GherkinDialectProvider.DIALECTS.keySet().each { language ->
    def dialect = dialectProvider.getDialect(language, null)
    def normalized_language = dialect.language.replaceAll("[\\s-]", "_").toLowerCase()
    if (!unsupported.contains(normalized_language)) {
        def templateSource = new File(project.baseDir, "src/main/groovy/lambda.java.gsp").getText()
        def className = "${normalized_language}".capitalize()
        def binding = [ "i18n":dialect, "className":className ]
        def template = engine.createTemplate(templateSource).make(binding)
        def file = new File(project.baseDir, "target/generated-sources/i18n/java/io/cucumber/java/api/lambda/${className}.java")
        file.parentFile.mkdirs()
        file.write(template.toString(), "UTF-8")
    }
}
