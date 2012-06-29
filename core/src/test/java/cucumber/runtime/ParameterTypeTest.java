package cucumber.runtime;

import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.runtime.converters.LocalizedXStreams;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ParameterTypeTest {

    private static final Locale LOCALE = Locale.US;
    private static final LocalizedXStreams.LocalizedXStream X = new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(LOCALE);

    public void withInt(int i) {
    }

    @Test
    public void converts_with_built_in_converter() throws NoSuchMethodException {
        ParameterType pt = ParameterType.fromMethod(getClass().getMethod("withInt", Integer.TYPE)).get(0);
        assertEquals(23, pt.convert("23", X, LOCALE));
    }

    public void withCustomTransform(@Transform(UppercasedTransformer.class) Uppercased uppercased) {
    }

    private class Uppercased {
        public String value;

        public Uppercased(String value) {

            this.value = value;
        }
    }

    private class UppercasedTransformer extends Transformer<Uppercased> {
        @Override
        public Uppercased transform(String value) {
            return new Uppercased(value.toUpperCase());
        }
    }

    @Test
    public void converts_with_custom_transform() throws NoSuchMethodException {
        ParameterType pt = ParameterType.fromMethod(getClass().getMethod("withCustomTransform", Uppercased.class)).get(0);
        assertEquals("HELLO", ((Uppercased) pt.convert("hello", X, LOCALE)).value);
    }
}
