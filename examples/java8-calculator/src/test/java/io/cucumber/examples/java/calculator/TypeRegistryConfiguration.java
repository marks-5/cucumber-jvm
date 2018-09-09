package io.cucumber.examples.java.calculator;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import io.cucumber.examples.java.calculator.RpnCalculatorStepdefs.Entry;
import io.cucumber.examples.java.calculator.ShoppingStepdefs.Grocery;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
            Entry.class,
            (Map<String, String> row) -> new Entry(
                Integer.valueOf(row.get("first")),
                Integer.valueOf(row.get("second")),
                row.get("operation")
            )
        ));

        typeRegistry.defineDataTableType(new DataTableType(
            Grocery.class,
            (Map<String, String> row) -> new Grocery(
                row.get("name"),
                ShoppingStepdefs.Price.fromString(row.get("price"))
            )
        ));
    }
}
