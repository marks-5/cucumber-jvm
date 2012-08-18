package cucumber.runtime;

/**
 * After Hooks that declare a parameter of this type will receive an instance of this class.
 * This allows an After hook to inspect whether or not a Scenario failed.
 */
public interface ScenarioResult {
    /**
     * @return the <em>most severe</em> status of the Scenario's Steps. One of "passed", "undefined", "pending", "skipped", "failed"
     */
    String getStatus();

    /**
     * @return true if and only if {@link #getStatus()} returns "failed"
     */
    boolean isFailed();

    /**
     * Embeds data into the report(s). Some reporters (such as the progress one) don't embed data, but others do (html and json).
     *
     * @param data     what to embed, for example an image.
     * @param mimeType what is the data?
     * @see cucumber.formatter.ProgressFormatter
     * @see cucumber.formatter.HTMLFormatter
     * @see gherkin.formatter.JSONFormatter
     */
    void embed(byte[] data, String mimeType);

    /**
     * Outputs some text into the report.
     *
     * @param text what to put in the report.
     */
    void write(String text);
}
