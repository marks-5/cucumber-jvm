package cucumber.table;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import cucumber.DateFormat;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Step;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;

public class FromDataTableTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final List<Argument> NO_ARGS = emptyList();
    private static final List<Comment> NO_COMMENTS = emptyList();

    public static class StepDefs {
        public List<UserPojo> listOfPojos;
        public List<UserBean> listOfBeans;
        public List<UserWithNameField> listOfUsersWithNameField;
        public List<List<Double>> listOfListOfDoubles;
        public List<Map<String, String>> listOfMapsOfStringToString;
        public List<Map<String, Object>> listOfMapsOfStringToObject;

        public DataTable dataTable;

        public void listOfPojos(@DateFormat("yyyy-MM-dd") List<UserPojo> listOfPojos) {
            this.listOfPojos = listOfPojos;
        }

        public void listOfBeans(@DateFormat("yyyy-MM-dd") List<UserBean> listOfBeans) {
            this.listOfBeans = listOfBeans;
        }

        public void listOfUsersWithNameField(@DateFormat("yyyy-MM-dd") List<UserWithNameField> listOfUsersWithNameField) {
            this.listOfUsersWithNameField = listOfUsersWithNameField;
        }

        public void listOfListOfDoubles(List<List<Double>> listOfListOfDoubles) {
            this.listOfListOfDoubles = listOfListOfDoubles;
        }

        public void listOfMapsOfStringToString(List<Map<String, String>> listOfMapsOfStringToString) {
            this.listOfMapsOfStringToString = listOfMapsOfStringToString;
        }

        public void listOfMapsOfStringToObject(List<Map<String, Object>> listOfMapsOfStringToObject) {
            this.listOfMapsOfStringToObject = listOfMapsOfStringToObject;
        }

        public void plainDataTable(DataTable dataTable) {
            this.dataTable = dataTable;
        }

        public void listOfMapsOfDateToString(List<Map<Date, String>> mapsOfDateToString) {
        }

        public void listOfMapsOfStringToDate(List<Map<String, Date>> mapsOfStringToDate) {
        }

        public void listOfMaps(List<Map> maps) {
        }
    }

    @Test
    public void transforms_to_list_of_pojos() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfPojos", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesAndCalWithHeader());
        assertEquals(sidsBirthday(), stepDefs.listOfPojos.get(0).birthDate);
        assertEquals(sidsDeathcal().getTime(), stepDefs.listOfPojos.get(0).deathCal.getTime());
    }

    @Test
    public void transforms_to_list_of_beans() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfBeans", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals(sidsBirthday(), stepDefs.listOfBeans.get(0).getBirthDate());
    }

    @Test
    public void converts_table_to_list_of_class_with_special_fields() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfUsersWithNameField", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesAndNamesWithHeader());
        assertEquals(sidsBirthday(), stepDefs.listOfUsersWithNameField.get(0).birthDate);
        assertEquals("Sid", stepDefs.listOfUsersWithNameField.get(0).name.first);
        assertEquals("Vicious", stepDefs.listOfUsersWithNameField.get(0).name.last);
    }

    @Test
    public void transforms_to_list_of_single_values() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoubles", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDoublesWithoutHeader());
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.listOfListOfDoubles.toString());
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_string() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfMapsOfStringToString", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals("1957-05-10", stepDefs.listOfMapsOfStringToString.get(0).get("Birth Date"));
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_object() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfMapsOfStringToObject", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals("1957-05-10", stepDefs.listOfMapsOfStringToObject.get(0).get("Birth Date"));
    }

    @Test
    public void passes_plain_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals("1957-05-10", stepDefs.dataTable.raw().get(1).get(0));
        assertEquals("Birth Date", stepDefs.dataTable.raw().get(0).get(0));
    }

    @Test
    public void does_not_transform_to_list_of_map_of_date_to_string() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to a List<Map<K,V>> when K is String. It was class java.util.Date.");

        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfDateToString", List.class);
        runStepDef(listOfBeans, listOfDatesWithHeader());
    }

    @Test
    public void does_not_transform_to_list_of_map_of_string_to_date() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to a List<Map<K,V>> when V is String or Object. It was class java.util.Date.");

        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfStringToDate", List.class);
        runStepDef(listOfBeans, listOfDatesWithHeader());
    }

    @Test
    public void does_not_transform_to_list_of_non_generic_map() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to List<Map<String,String>> or List<Map<String,Object>>. You have to declare generic types.");

        Method listOfBeans = StepDefs.class.getMethod("listOfMaps", List.class);
        runStepDef(listOfBeans, listOfDatesWithHeader());
    }

    private StepDefs runStepDef(Method method, List<DataTableRow> rows) throws Throwable {
        StepDefs stepDefs = new StepDefs();
        StepDefinition stepDefinition = new DirectStepDef(stepDefs, method);

        Step stepWithRows = new Step(NO_COMMENTS, "Given ", "something", 10, rows, null);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(NO_ARGS, stepDefinition, "some.feature", stepWithRows, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(new I18n("en"));
        return stepDefs;
    }

    private List<DataTableRow> listOfDatesWithHeader() {
        List<DataTableRow> rows = new ArrayList<DataTableRow>();
        rows.add(new DataTableRow(NO_COMMENTS, asList("Birth Date"), 1));
        rows.add(new DataTableRow(NO_COMMENTS, asList("1957-05-10"), 2));
        return rows;
    }

    private List<DataTableRow> listOfDatesAndCalWithHeader() {
        List<DataTableRow> rows = new ArrayList<DataTableRow>();
        rows.add(new DataTableRow(NO_COMMENTS, asList("Birth Date", "Death Cal"), 1));
        rows.add(new DataTableRow(NO_COMMENTS, asList("1957-05-10", "1979-02-02"), 2));
        return rows;
    }

    private List<DataTableRow> listOfDatesAndNamesWithHeader() {
        List<DataTableRow> rows = new ArrayList<DataTableRow>();
        rows.add(new DataTableRow(NO_COMMENTS, asList("Birth Date", "Name"), 1));
        rows.add(new DataTableRow(NO_COMMENTS, asList("1957-05-10", "Sid Vicious"), 2));
        return rows;
    }

    private List<DataTableRow> listOfDoublesWithoutHeader() {
        List<DataTableRow> rows = new ArrayList<DataTableRow>();
        rows.add(new DataTableRow(NO_COMMENTS, asList("100.5", "99.5"), 2));
        rows.add(new DataTableRow(NO_COMMENTS, asList("0.5", "-0.5"), 2));
        rows.add(new DataTableRow(NO_COMMENTS, asList("1000", "999"), 2));
        return rows;
    }

    private Date sidsBirthday() {
        Calendar sidsBirthday = Calendar.getInstance();
        sidsBirthday.set(1957, 4, 10, 0, 0, 0);
        sidsBirthday.set(Calendar.MILLISECOND, 0);
        sidsBirthday.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sidsBirthday.getTime();
    }

    private Calendar sidsDeathcal() {
        Calendar sidsDeathcal = Calendar.getInstance();
        sidsDeathcal.set(1979, 1, 2, 0, 0, 0);
        sidsDeathcal.set(Calendar.MILLISECOND, 0);
        sidsDeathcal.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sidsDeathcal;
    }

    public static class UserPojo {
        private Date birthDate;
        private Calendar deathCal;
    }

    @XStreamConverter(JavaBeanConverter.class)
    public static class UserBean {
        private Date birthDateX;

        public Date getBirthDate() {
            return this.birthDateX;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDateX = birthDate;
        }
    }

    public static class UserWithNameField {
        public Name name;
        public Date birthDate;
    }

    @XStreamConverter(NameConverter.class)
    public static class Name {
        public String first;
        public String last;
    }

    public static class NameConverter implements Converter {
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            Name name = new Name();
            String[] firstLast = reader.getValue().split(" ");
            name.first = firstLast[0];
            name.last = firstLast[1];
            return name;
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(Name.class);
        }
    }

    private class DirectStepDef implements StepDefinition {
        private final Object target;
        private final Method method;

        public DirectStepDef(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        @Override
        public List<Argument> matchedArguments(Step step) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocation() {
            return getClass().getName();
        }

        @Override
        public List<ParameterType> getParameterTypes() {
            return ParameterType.fromMethod(method);
        }

        @Override
        public void execute(I18n i18n, Object[] args) throws Throwable {
            method.invoke(target, args);
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getPattern() {
            throw new UnsupportedOperationException();
        }
    }
}
