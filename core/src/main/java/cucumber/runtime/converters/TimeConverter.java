package cucumber.runtime.converters;

import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterType;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.asList;

public abstract class TimeConverter<T> extends ConverterWithFormat<T> {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    protected final Locale locale;
    private final List<DateFormat> formats = new ArrayList<DateFormat>();
    private SimpleDateFormat onlyFormat;

    public TimeConverter(Locale locale, Class[] convertibleTypes) {
        super(convertibleTypes);
        this.locale = locale;

        // TODO - these are expensive to create. Cache by format+string, or use the XStream DF cache util thingy
        addFormat(DateFormat.SHORT, locale);
        addFormat(DateFormat.MEDIUM, locale);
        addFormat(DateFormat.LONG, locale);
        addFormat(DateFormat.FULL, locale);
    }

    protected void addFormat(int style, Locale locale) {
        add(DateFormat.getDateInstance(style, locale));
    }

    protected void add(DateFormat dateFormat) {
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(UTC);
        formats.add(dateFormat);
    }

    public List<? extends Format> getFormats() {
        return onlyFormat == null ? formats : asList(onlyFormat);
    }

    public void setOnlyFormat(String dateFormatString, Locale locale) {
        onlyFormat = new SimpleDateFormat(dateFormatString, locale);
        onlyFormat.setLenient(false);
        onlyFormat.setTimeZone(UTC);
    }

    public void removeOnlyFormat() {
        onlyFormat = null;
    }

    public static TimeConverter getInstance(ParameterType parameterType, Locale locale) {
        if (Date.class.isAssignableFrom(parameterType.getParameterClass())) {
            return new DateConverter(locale);
        } else if (Calendar.class.isAssignableFrom(parameterType.getParameterClass())) {
            return new CalendarConverter(locale);
        } else {
            throw new CucumberException("Unsupported time type: " + parameterType.getParameterClass());
        }
    }

    public static List<Class> getTimeClasses() {
        List<Class> classes = new ArrayList<Class>();
        classes.add(Date.class);
        classes.add(Calendar.class);
        return classes;
    }
}
