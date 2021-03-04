package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.datasourceformat.DataSourceFormat;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 *
 */
class FieldOptionsTest {
    private List<String> trueValues = Arrays.asList("agreed", "yep!");
    private List<String> falseValues = Arrays.asList("disagreed", "nope!");

    @Test
    @DisplayName("Create BooleanField with custom true/false options")
    void testBooleanFieldCreationFromString() {
        Map<String, Object> options = new HashMap<>();
        options.put("trueValues", trueValues);
        options.put("falseValues", falseValues);

        Field testField = new BooleanField("name", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, options);

        assertThrows(InvalidCastException.class, () -> {
            testField.castValue("true", false, options);
        });
        testField.castValue("agreed", false, options);
    }


    @Test
    @DisplayName("Test Field options getter/setter")
    void testFieldOptionsGetterSetter() {
        Map<String, Object> options = new HashMap<>();
        options.put("trueValues", trueValues);
        options.put("falseValues", falseValues);

        Field testField = new BooleanField("name", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);

        testField.setOptions(options);
        Assertions.assertEquals(options, testField.getOptions());
    }

    @Test
    @DisplayName("Create BooleanField with custom true/false options from Schema")
    public void testReadSchemaBooleanFieldOptions() throws Exception{
        File source = TestHelper.getResourceFile("/fixtures/schema/employee_schema_boolean_" +
                "alternative_values.json");
        Schema schema = Schema.fromJson(source, true);
        File file = new File("data/employee_data.csv");
        Table table = Table.fromSource(file, getTestDataDirectory(), schema, DataSourceFormat.getDefaultCsvFormat());

        Path tempDirPath = Files.createTempDirectory("datapackage-");
        table.writeCsv(new File(tempDirPath.toFile(), "table.csv"), null);

        Table table2 = Table.fromSource(
                new File("table.csv"),
                tempDirPath.toFile(),
                schema,
                DataSourceFormat.getDefaultCsvFormat());

        Assertions.assertEquals(table, table2);
    }

    @Test
    @DisplayName("Create BooleanField with invalid true/false options from CSV")
    void testBooleanFieldCreationInvalid() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema schema;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }
        File file = new File("data/employee_data_alternative_boolean.csv");
        Table table = Table.fromSource(file, getTestDataDirectory(), schema, DataSourceFormat.getDefaultCsvFormat());

        assertThrows(InvalidCastException.class, () -> {
            table.read(true);
        });
    }
}
