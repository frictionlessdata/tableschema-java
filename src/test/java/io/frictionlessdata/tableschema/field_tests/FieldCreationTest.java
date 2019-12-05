package io.frictionlessdata.tableschema.field_tests;

import io.frictionlessdata.tableschema.field.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

/**
 *
 * 
 */
class FieldCreationTest {

    @Test
    @DisplayName("Create AnyField")
    void testAnyFieldCreation() throws Exception{
        Field testField = new AnyField("anon");
        Assertions.assertEquals(testField.getName(), "anon");
        Assertions.assertEquals(testField.getType(), "any");
    }

    @Test
    @DisplayName("Create AnyField, full constructor")
    void testAnyFieldCreation2() throws Exception{
        Field testField = new AnyField("anon", Field.FIELD_FORMAT_DEFAULT, "title", "descriptions",
                new URI("https://github.com"), null, null);
        Assertions.assertEquals( "anon", testField.getName());
        Assertions.assertEquals( "any", testField.getType());
        Assertions.assertEquals("title", testField.getTitle());
        Assertions.assertEquals("descriptions", testField.getDescription());
        Assertions.assertEquals(new URI("https://github.com"), testField.getRdfType());
        Assertions.assertNull(testField.getConstraints());
        Assertions.assertNull(testField.getOptions());
    }

    @Test
    @DisplayName("Create AnyField from JSON")
    void testAnyFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"anon\",\"format\":\"\",\"description\":\"\"," +
                "\"title\":\"\",\"type\":\"any\",\"constraints\":{}}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "anon");
        Assertions.assertEquals(testField.getType(), "any");
    }

    @Test
    @DisplayName("Create ArrayField")
    void testArrayFieldCreation() throws Exception{
        Field testField = new ArrayField("employees");
        Assertions.assertEquals(testField.getName(), "employees");
        Assertions.assertEquals(testField.getType(), "array");
    }

    @Test
    @DisplayName("Create ArrayField from JSON")
    void testArrayFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"employees\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"array\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "employees");
        Assertions.assertEquals(testField.getType(), "array");
    }

    @Test
    @DisplayName("Create BooleanField")
    void testBooleanFieldCreation() throws Exception{
        Field testField = new BooleanField("is_valid");
        Assertions.assertEquals(testField.getName(), "is_valid");
        Assertions.assertEquals(testField.getType(), "boolean");
    }

    @Test
    @DisplayName("Create BooleanField from JSON")
    void testBooleanFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"is_valid\",\"format\":\"default\",\"description\":\"\"" +
                ",\"type\":\"boolean\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "is_valid");
        Assertions.assertEquals(testField.getType(), "boolean");
    }

    @Test
    @DisplayName("Create DateField")
    void testDateFieldCreation() throws Exception{
        String fieldName = "today";
        Field testField = new DateField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "date");
    }

    @Test
    @DisplayName("Create DateField from JSON")
    void testDateFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"today\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"date\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "today");
        Assertions.assertEquals(testField.getType(), "date");
    }

    @Test
    @DisplayName("Create DatetimeField")
    void testDatetimeFieldCreation() throws Exception{
        String fieldName = "today_noon";
        Field testField = new DatetimeField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "datetime");
    }

    @Test
    @DisplayName("Create DatetimeField from JSON")
    void testDatetimeieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"today_noon\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"datetime\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "today_noon");
        Assertions.assertEquals(testField.getType(), "datetime");
    }

    @Test
    @DisplayName("Create DurationField")
    void testDurationFieldCreation() throws Exception{
        String fieldName = "aday";
        Field testField = new DurationField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "duration");
    }

    @Test
    @DisplayName("Create DatetimeField from JSON")
    void testDurationFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"aday\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"duration\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "aday");
        Assertions.assertEquals(testField.getType(), "duration");
    }

    @Test
    @DisplayName("Create GeojsonField")
    void testGeojsonFieldCreation() throws Exception{
        String fieldName = "latlong";
        Field testField = new GeojsonField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "geojson");
    }

    @Test
    @DisplayName("Create GeojsonField from JSON")
    void testGeojsonFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"latlong\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"geojson\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "latlong");
        Assertions.assertEquals(testField.getType(), "geojson");
    }

    @Test
    @DisplayName("Create GeopointField")
    void testGeopointFieldCreation() throws Exception{
        String fieldName = "latlong";
        Field testField = new GeopointField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "geopoint");
    }

    @Test
    @DisplayName("Create GeopointField from JSON")
    void testGeopointFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"latlong\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"geopoint\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "latlong");
        Assertions.assertEquals(testField.getType(), "geopoint");
    }

    @Test
    @DisplayName("Create IntegerField")
    void testIntegerFieldCreation() throws Exception{
        String fieldName = "int";
        Field testField = new IntegerField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "integer");
    }

    @Test
    @DisplayName("Create IntegerField from JSON")
    void testIntegerFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"int\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"integer\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "int");
        Assertions.assertEquals(testField.getType(), "integer");
    }

    @Test
    @DisplayName("Create NumberField")
    void testNumberFieldCreation() throws Exception{
        String fieldName = "number";
        Field testField = new NumberField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "number");
    }

    @Test
    @DisplayName("Create NumberField from JSON")
    void testNumberFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"number\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"number\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "number");
        Assertions.assertEquals(testField.getType(), "number");
    }

    @Test
    @DisplayName("Create ObjectField")
    void testObjectFieldCreation() throws Exception{
        String fieldName = "obj";
        Field testField = new ObjectField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "object");
    }

    @Test
    @DisplayName("Create ObjectField from JSON")
    void testObjectFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"obj\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"object\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "obj");
        Assertions.assertEquals(testField.getType(), "object");
    }

    @Test
    @DisplayName("Create StringField")
    void testStringFieldCreation() throws Exception{
        Field testField = new StringField("city");
        Assertions.assertEquals(testField.getName(), "city");
        Assertions.assertEquals(testField.getType(), "string");
    }

    @Test
    @DisplayName("Create StringField from JSON")
    void testStringFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"city\",\"format\":\"\",\"description\":\"\"," +
                "\"title\":\"\",\"type\":\"string\",\"constraints\":{}}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "city");
        Assertions.assertEquals(testField.getType(), "string");
    }

    @Test
    @DisplayName("Create TimeField")
    void testTimeFieldCreation() throws Exception{
        String fieldName = "noon";
        Field testField = new TimeField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "time");
    }

    @Test
    @DisplayName("Create TimeField from JSON")
    void testTimeFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"noon\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"time\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "noon");
        Assertions.assertEquals(testField.getType(), "time");
    }

    @Test
    @DisplayName("Create YearField")
    void testYearFieldCreation() throws Exception{
        String fieldName = "1997";
        Field testField = new YearField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "year");
    }

    @Test
    @DisplayName("Create YearField from JSON")
    void testYearFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"1997\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"year\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "1997");
        Assertions.assertEquals(testField.getType(), "year");
    }

    @Test
    @DisplayName("Create YearmonthField")
    void testYearmonthFieldFieldCreation() throws Exception{
        String fieldName = "dec1997";
        Field testField = new YearmonthField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "yearmonth");
    }

    @Test
    @DisplayName("Create YearmonthField from JSON")
    void testYearmonthFieldFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"dec1997\",\"format\":\"default\",\"description\":\"\"" +
                ",\"type\":\"yearmonth\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertEquals(testField.getName(), "dec1997");
        Assertions.assertEquals(testField.getType(), "yearmonth");
    }

    @Test
    @DisplayName("Test undefined Field type creation")
    void testUndefinedFieldCreation() throws Exception{
        String type = "anon";
        String name = "anon";
        Field testField = Field.forType(type, name);
        Assertions.assertTrue(testField instanceof AnyField);
        Assertions.assertEquals(testField.getName(), "anon");
        Assertions.assertEquals(testField.getType(), "any");
    }

    @Test
    @DisplayName("Test undefined Field type creation from JSON")
    void testUndefinedFieldFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"anon\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"anon\",\"title\":\"\"}";
        Field testField = Field.fromJson(testJson);
        Assertions.assertTrue(testField instanceof AnyField);
        Assertions.assertEquals(testField.getName(), "anon");
        Assertions.assertEquals(testField.getType(), "any");
    }

}
