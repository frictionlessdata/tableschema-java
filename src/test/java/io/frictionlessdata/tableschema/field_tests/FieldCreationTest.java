package io.frictionlessdata.tableschema.field_tests;

import io.frictionlessdata.tableschema.field.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * 
 */
public class FieldCreationTest {

    @Test
    @DisplayName("Create StringField")
    public void testStringFieldCreation() throws Exception{
        Field testField = new StringField("city");
        Assertions.assertEquals(testField.getName(), "city");
        Assertions.assertEquals(testField.getType(), "string");
    }

    @Test
    @DisplayName("Create StringField from JSON")
    public void testStringFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"city\",\"format\":\"\",\"description\":\"\"," +
                "\"title\":\"\",\"type\":\"string\",\"constraints\":{}}";
        Field testField = new StringField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "city");
        Assertions.assertEquals(testField.getType(), "string");
    }

    @Test
    @DisplayName("Create ArrayField")
    public void testArrayFieldCreation() throws Exception{
        Field testField = new ArrayField("employees");
        Assertions.assertEquals(testField.getName(), "employees");
        Assertions.assertEquals(testField.getType(), "array");
    }

    @Test
    @DisplayName("Create ArrayField from JSON")
    public void testArrayFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"employees\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"array\",\"title\":\"\"}";
        Field testField = new ArrayField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "employees");
        Assertions.assertEquals(testField.getType(), "array");
    }

    @Test
    @DisplayName("Create BooleanField")
    public void testBooleanFieldCreation() throws Exception{
        Field testField = new BooleanField("is_valid");
        Assertions.assertEquals(testField.getName(), "is_valid");
        Assertions.assertEquals(testField.getType(), "boolean");
    }

    @Test
    @DisplayName("Create BooleanField from JSON")
    public void testBooleanFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"is_valid\",\"format\":\"default\",\"description\":\"\"" +
                ",\"type\":\"boolean\",\"title\":\"\"}";
        Field testField = new BooleanField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "is_valid");
        Assertions.assertEquals(testField.getType(), "boolean");
    }

    @Test
    @DisplayName("Create DateField")
    public void testDateFieldCreation() throws Exception{
        String fieldName = "today";
        Field testField = new DateField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "date");
    }

    @Test
    @DisplayName("Create DateField from JSON")
    public void testDateFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"today\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"date\",\"title\":\"\"}";
        Field testField = new DateField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "today");
        Assertions.assertEquals(testField.getType(), "date");
    }

    @Test
    @DisplayName("Create DatetimeField")
    public void testDatetimeFieldCreation() throws Exception{
        String fieldName = "today_noon";
        Field testField = new DatetimeField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "datetime");
    }

    @Test
    @DisplayName("Create DatetimeField from JSON")
    public void testDatetimeieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"today_noon\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"datetime\",\"title\":\"\"}";
        Field testField = new DatetimeField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "today_noon");
        Assertions.assertEquals(testField.getType(), "datetime");
    }

    @Test
    @DisplayName("Create DurationField")
    public void testDurationFieldCreation() throws Exception{
        String fieldName = "aday";
        Field testField = new DurationField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "duration");
    }

    @Test
    @DisplayName("Create DatetimeField from JSON")
    public void testDurationFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"aday\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"duration\",\"title\":\"\"}";
        Field testField = new DurationField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "aday");
        Assertions.assertEquals(testField.getType(), "duration");
    }

    @Test
    @DisplayName("Create GeojsonField")
    public void testGeojsonFieldCreation() throws Exception{
        String fieldName = "latlong";
        Field testField = new GeojsonField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "geojson");
    }

    @Test
    @DisplayName("Create GeojsonField from JSON")
    public void testGeojsonFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"latlong\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"geojson\",\"title\":\"\"}";
        Field testField = new GeojsonField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "latlong");
        Assertions.assertEquals(testField.getType(), "geojson");
    }

    @Test
    @DisplayName("Create GeopointField")
    public void testGeopointFieldCreation() throws Exception{
        String fieldName = "latlong";
        Field testField = new GeopointField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "geopoint");
    }

    @Test
    @DisplayName("Create GeopointField from JSON")
    public void testGeopointFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"latlong\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"geopoint\",\"title\":\"\"}";
        Field testField = new GeopointField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "latlong");
        Assertions.assertEquals(testField.getType(), "geopoint");
    }

    @Test
    @DisplayName("Create IntegerField")
    public void testIntegerFieldCreation() throws Exception{
        String fieldName = "int";
        Field testField = new IntegerField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "integer");
    }

    @Test
    @DisplayName("Create IntegerField from JSON")
    public void testIntegerFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"int\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"integer\",\"title\":\"\"}";
        Field testField = new IntegerField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "int");
        Assertions.assertEquals(testField.getType(), "integer");
    }

    @Test
    @DisplayName("Create NumberField")
    public void testNumberFieldCreation() throws Exception{
        String fieldName = "number";
        Field testField = new NumberField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "number");
    }

    @Test
    @DisplayName("Create NumberField from JSON")
    public void testNumberFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"number\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"number\",\"title\":\"\"}";
        Field testField = new NumberField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "number");
        Assertions.assertEquals(testField.getType(), "number");
    }

    @Test
    @DisplayName("Create ObjectField")
    public void testObjectFieldCreation() throws Exception{
        String fieldName = "obj";
        Field testField = new ObjectField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "object");
    }

    @Test
    @DisplayName("Create ObjectField from JSON")
    public void testObjectFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"obj\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"object\",\"title\":\"\"}";
        Field testField = new ObjectField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "obj");
        Assertions.assertEquals(testField.getType(), "object");
    }

    @Test
    @DisplayName("Create TimeField")
    public void testTimeFieldCreation() throws Exception{
        String fieldName = "noon";
        Field testField = new TimeField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "time");
    }

    @Test
    @DisplayName("Create TimeField from JSON")
    public void testTimeFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"noon\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"time\",\"title\":\"\"}";
        Field testField = new TimeField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "noon");
        Assertions.assertEquals(testField.getType(), "time");
    }

    @Test
    @DisplayName("Create YearField")
    public void testYearFieldCreation() throws Exception{
        String fieldName = "1997";
        Field testField = new YearField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "year");
    }

    @Test
    @DisplayName("Create YearField from JSON")
    public void testYearFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"1997\",\"format\":\"default\",\"description\":\"\"," +
                "\"type\":\"year\",\"title\":\"\"}";
        Field testField = new YearField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "1997");
        Assertions.assertEquals(testField.getType(), "year");
    }

    @Test
    @DisplayName("Create YearmonthField")
    public void testYearmonthFieldFieldCreation() throws Exception{
        String fieldName = "dec1997";
        Field testField = new YearmonthField(fieldName);
        Assertions.assertEquals(testField.getName(), fieldName);
        Assertions.assertEquals(testField.getType(), "yearmonth");
    }

    @Test
    @DisplayName("Create YearmonthField from JSON")
    public void testYearmonthFieldFieldCreationFromString() throws Exception{
        String testJson = "{\"name\":\"dec1997\",\"format\":\"default\",\"description\":\"\"" +
                ",\"type\":\"yearmonth\",\"title\":\"\"}";
        Field testField = new YearmonthField(new JSONObject(testJson));
        Assertions.assertEquals(testField.getName(), "dec1997");
        Assertions.assertEquals(testField.getType(), "yearmonth");
    }
}
