package io.frictionlessdata.tableschema.field_tests;

import io.frictionlessdata.tableschema.Schema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.field.*;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
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
    @DisplayName("Create BooleanField with true/false options from CSV")
    void testBooleanFieldCreation() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema schema;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = new Schema(fis, false);
        }
        File file = new File("data/employee_data_alternative_boolean.csv");
        Table table = new Table(file, getTestDataDirectory(), schema);
        Map<String, Object> options = new HashMap<>();
        options.put("trueValues", trueValues);
        options.put("falseValues", falseValues);
        table.setFieldOptions(options);

        List<Object[]> data = table.read(true);
        Assertions.assertTrue((Boolean)data.get(0)[3]);
        Assertions.assertTrue((Boolean)data.get(1)[3]);
        Assertions.assertFalse((Boolean)data.get(2)[3]);
        Assertions.assertFalse((Boolean)data.get(3)[3]);
    }

    @Test
    @DisplayName("Create BooleanField with custom true/false options")
    void testBooleanFieldCreationFromString() {
        Map<String, Object> options = new HashMap<>();
        options.put("trueValues", trueValues);
        options.put("falseValues", falseValues);

        Field testField = new BooleanField("name", "default", null, null, null, options);

        assertThrows(InvalidCastException.class, () -> {
            testField.castValue("true", false, options);
        });
        testField.castValue("agreed", false, options);
    }

    @Test
    @DisplayName("Create BooleanField with invalid true/false options from CSV")
    void testBooleanFieldCreationInvalid() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema schema;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = new Schema(fis, false);
        }
        File file = new File("data/employee_data_alternative_boolean.csv");
        Table table = new Table(file, getTestDataDirectory(), schema);

        assertThrows(InvalidCastException.class, () -> {
            table.read(true);
        });
    }
}
