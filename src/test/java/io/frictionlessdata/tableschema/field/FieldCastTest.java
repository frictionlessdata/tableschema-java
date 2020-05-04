package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.databind.JsonNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.datasourceformat.DataSourceFormat;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.schema.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 *
 */
class FieldCastTest {


    @Test
    void testFieldCastGeopointDefault() throws Exception{
        GeopointField field = new GeopointField("test", Field.FIELD_FORMAT_DEFAULT, "title", "description", null, null, null);
        double[] val = field.castValue("0.00012,21");
        Assertions.assertEquals(0.00012, val[0]);
        Assertions.assertEquals(21, val[1]);
    }

    @Test
    void testFieldCastGeopointArray() throws Exception{
        GeopointField field = new GeopointField("test", Field.FIELD_FORMAT_ARRAY, "title", "description", null, null, null);
        double[] val = field.castValue("[45,32.54]");
        Assertions.assertEquals(45, val[0]);
        Assertions.assertEquals(32.54, val[1]);
    }

    @Test
    void testFieldCastGeopointObject() throws Exception{
        GeopointField field = new GeopointField("test", Field.FIELD_FORMAT_OBJECT, Field.FIELD_FORMAT_DEFAULT, null, null, null, null);
        double[] val = field.castValue("{\"lon\": 67.123, \"lat\": 19}");
        Assertions.assertEquals(67.123, val[0]);
        Assertions.assertEquals(19, val[1]);
    }

    @Test
    void testFieldCastInteger() throws Exception{
        IntegerField field = new IntegerField("test");
        BigInteger val = field.castValue("123");
        Assertions.assertEquals(123, val.intValue());
    }

    @Test
    void testFieldCastDuration() throws Exception{
        DurationField field = new DurationField("test");
        Duration val = field.castValue("P2DT3H4M");
        Assertions.assertEquals(183840, val.getSeconds());
    }

    @Test
    void testFieldCastValidGeojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_DEFAULT, Field.FIELD_FORMAT_DEFAULT, null, null, null, null);
        JsonNode val = field.castValue("{\n" +
            "    \"type\": \"Feature\",\n" +
            "    \"properties\": {\n" +
            "        \"name\": \"Coors Field\",\n" +
            "        \"amenity\": \"Baseball Stadium\",\n" +
            "        \"popupContent\": \"This is where the Rockies play!\"\n" +
            "    },\n" +
            "    \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [-104.99404, 39.75621]\n" +
            "    }\n" +
            "}");

        Assertions.assertEquals("Feature", val.get("type").asText());
        Assertions.assertEquals("Baseball Stadium", val.get("properties").get("amenity").asText());
        Assertions.assertEquals(-104.99404, val.get("geometry").get("coordinates").get(0).asDouble());
        Assertions.assertEquals(39.75621, val.get("geometry").get("coordinates").get(1).asDouble());
    }

    @Test
    void testFieldCastInvalidGeojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_DEFAULT, Field.FIELD_FORMAT_DEFAULT, null, null, null, null);
        assertThrows(InvalidCastException.class, () -> {
            field.castValue("{\n" +
                "    \"type\": \"INVALID_TYPE\",\n" + // The invalidity is here.
                "    \"properties\": {\n" +
                "        \"name\": \"Coors Field\",\n" +
                "        \"amenity\": \"Baseball Stadium\",\n" +
                "        \"popupContent\": \"This is where the Rockies play!\"\n" +
                "    },\n" +
                "    \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [-104.99404, 39.75621]\n" +
                "    }\n" +
                "}");
        });

    }

    @Test
    void testFieldCastValidTopojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_TOPOJSON, Field.FIELD_FORMAT_DEFAULT, null, null, null, null);

        JsonNode val = field.castValue("{\n" +
            "  \"type\": \"Topology\",\n" +
            "  \"transform\": {\n" +
            "    \"scale\": [0.036003600360036005, 0.017361589674592462],\n" +
            "    \"translate\": [-180, -89.99892578124998]\n" +
            "  },\n" +
            "  \"objects\": {\n" +
            "    \"aruba\": {\n" +
            "      \"type\": \"Polygon\",\n" +
            "      \"arcs\": [[0]],\n" +
            "      \"id\": 533\n" +
            "    }\n" +
            "  },\n" +
            "  \"arcs\": [\n" +
            "    [[3058, 5901], [0, -2], [-2, 1], [-1, 3], [-2, 3], [0, 3], [1, 1], [1, -3], [2, -5], [1, -1]]\n" +
            "  ]\n" +
            "}");

        Assertions.assertEquals("Topology", val.get("type").asText());
        Assertions.assertEquals(0.036003600360036005, val.get("transform").get("scale").get(0).asDouble());
        Assertions.assertEquals(0.017361589674592462, val.get("transform").get("scale").get(1).asDouble());
        Assertions.assertEquals(-180, val.get("transform").get("translate").get(0).asDouble());
        Assertions.assertEquals(-89.99892578124998, val.get("transform").get("translate").get(1).asDouble());

        /*
        // Another Geosjon to test
        JSONObject val2 = field.castValue("{ \"type\": \"GeometryCollection\",\n" +
            "\"geometries\": [\n" +
            "  { \"type\": \"Point\",\n" +
            "    \"coordinates\": [100.0, 0.0]\n" +
            "    },\n" +
            "  { \"type\": \"LineString\",\n" +
            "    \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n" +
            "    }\n" +
            " ]\n" +
            "}");

        Assertions.assertEquals("GeometryCollection", val.getString("type"));
        Assertions.assertEquals("Point", val.getJSONArray("geometries").getJSONObject(0).getString("type"));
        Assertions.assertEquals("LineString", val.getJSONArray("geometries").getJSONObject(1).getString("type"));
        */
    }

    @Test
    void testFieldCastInvalidTopojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_TOPOJSON, Field.FIELD_FORMAT_DEFAULT, null, null, null, null);

        // This is an invalid Topojson, it's a Geojson:
        assertThrows(InvalidCastException.class, () -> {
            field.castValue("{ \"type\": \"GeometryCollection\",\n" +
                    "\"geometries\": [\n" +
                    "  { \"type\": \"Point\",\n" +
                    "    \"coordinates\": [100.0, 0.0]\n" +
                    "    },\n" +
                    "  { \"type\": \"LineString\",\n" +
                    "    \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n" +
                    "    }\n" +
                    " ]\n" +
                    "}");
        });
    }

    @Test
    void testCastNumberGroupChar() throws Exception{
        String testValue = "1 564 1020";
        Map<String, Object> options = new HashMap<>();
        options.put("groupChar", " ");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assertions.assertEquals(15641020L, num.intValue());
    }

    @Test
    void testCastNumberDecimalChar() throws Exception{
        String testValue = "1020,123";
        Map<String, Object> options = new HashMap();
        options.put("decimalChar", ",");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assertions.assertEquals(1020.123, num.floatValue(), 0.01);
    }

    @Test
    void testCastNumberNonBare() throws Exception{
        String testValue = "150 EUR";
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);

        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);
        Assertions.assertEquals(150, num.intValue());

        testValue = "$125";
        num = field.castValue(testValue, false, options);
        Assertions.assertEquals(125, num.intValue());
    }

    @Test
    void testCastNumberGroupAndDecimalCharAsWellAsNonBare() throws Exception{
        String testValue = "1 564,123 EUR";
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);
        options.put("groupChar", " ");
        options.put("decimalChar", ",");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);
        Assertions.assertEquals(1564.123, num.floatValue(), 0.01);

    }


    @Test
    void testFieldCastObject() throws Exception{
        ObjectField field = new ObjectField("test");
        Map<String, Object> val = field.castValue("{\"one\": 1, \"two\": 2, \"three\": 3}");
        Assertions.assertEquals(3, val.size());
        Assertions.assertEquals(1, val.get("one"));
        Assertions.assertEquals(2, val.get("two"));
        Assertions.assertEquals(3, val.get("three"));
    }

    @Test
    void testFieldCastArray() throws Exception{
        ArrayField field = new ArrayField("test");
        Object[] val = field.castValue("[1,2,3,4]");

        Assertions.assertEquals(4, val.length);
        Assertions.assertEquals(1, val[0]);
        Assertions.assertEquals(2, val[1]);
        Assertions.assertEquals(3, val[2]);
        Assertions.assertEquals(4, val[3]);
    }

    @Test
    void testFieldCastDateTime() throws Exception{
        DatetimeField field = new DatetimeField("test");
        ZonedDateTime val = field.castValue("2008-08-30T01:45:36.123Z");

        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthValue());
        Assertions.assertEquals(30, val.getDayOfMonth());
        Assertions.assertEquals(1, val.getHour());
        Assertions.assertEquals(45, val.getMinute());
        Assertions.assertEquals(36, val.getSecond());
        Assertions.assertEquals(123000000, val.getNano());
        Assertions.assertEquals("2008-08-30T01:45:36.123Z", val.toString());
    }

    @Test
    void testFieldCastDate() throws Exception{
        DateField field = new DateField("test");
        LocalDate val = field.castValue("2008-08-30");

        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthValue());
        Assertions.assertEquals(30, val.getDayOfMonth());
    }

    @Test
    void testFieldCastTime() throws Exception{
        TimeField field = new TimeField("test");
        LocalTime val = field.castValue("14:22:33");

        Assertions.assertEquals(14, val.getHour());
        Assertions.assertEquals(22, val.getMinute());
        Assertions.assertEquals(33, val.getSecond());
    }

    @Test
    void testFieldCastYear() throws Exception{
        YearField field = new YearField("test");
        Year val = field.castValue("2008");
        Assertions.assertEquals(2008, val.getValue());
    }

    @Test
    void testFieldCastYearmonth() throws Exception{
        YearmonthField field = new YearmonthField("test");
        YearMonth val = field.castValue("2008-08");

        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthValue());
    }

    @Test
    void testFieldCastNumber() throws Exception{
        IntegerField intField = new IntegerField("intNum");
        NumberField floatField = new NumberField("floatNum");

        BigInteger intValPositive1 = intField.castValue("123");
        Assertions.assertEquals(123, intValPositive1.intValue());

        BigInteger intValPositive2 = intField.castValue("+128127");
        Assertions.assertEquals(128127, intValPositive2.intValue());

        BigInteger intValNegative = intField.castValue("-765");
        Assertions.assertEquals(-765, intValNegative.intValue());

        Number floatValPositive1 = floatField.castValue("123.9902");
        Assertions.assertEquals(123.9902, floatValPositive1.floatValue(), 0.01);

        Number floatValPositive2 = floatField.castValue("+128127.1929");
        Assertions.assertEquals(128127.1929, floatValPositive2.floatValue(), 0.01);

        Number floatValNegative = floatField.castValue("-765.929");
        Assertions.assertEquals(-765.929, floatValNegative.floatValue(), 0.01);

    }

    @Test
    void testFieldCastBoolean() throws Exception{
        BooleanField field = new BooleanField("test");
        Assertions.assertFalse(field.castValue("False"));
        Assertions.assertFalse(field.castValue("false"));
        Assertions.assertFalse(field.castValue("FALSE"));

        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("f"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("F"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("no"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("NO"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("n"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("N"));
        });

        Assertions.assertTrue(field.castValue("True"));
        Assertions.assertTrue(field.castValue("true"));
        Assertions.assertTrue(field.castValue("TRUE"));
        Assertions.assertTrue(field.castValue("1"));


        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("t"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("T"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("yes"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("YES"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("y"));
        });
        assertThrows(InvalidCastException.class, () -> {
            Assertions.assertFalse(field.castValue("Y"));
        });
    }

    @Test
    void testFieldCastString() throws Exception{
        StringField field = new StringField("test");
        String val = field.castValue("John Doe");

        Assertions.assertEquals("John Doe", val);
    }

    @Test
    @DisplayName("Test fix for Issue https://github.com/frictionlessdata/tableschema-java/issues/21")
    void testIssue21() {
        IntegerField intField = new IntegerField("intNum");
        NumberField floatField = new NumberField("floatNum");

        BigInteger intVal = intField.castValue("16289212000");
        Number floatVal = floatField.castValue("16289212000.0");
        Assertions.assertTrue(floatVal instanceof BigDecimal);
        Assertions.assertEquals(((BigDecimal)floatVal).toBigInteger(), intVal);
    }

    @Test
    @DisplayName("Test fix for Issue https://github.com/frictionlessdata/tableschema-java/issues/21")
    void test2Issue21() throws Exception{
        File f = new File("data/gdp.csv");
        File schemaFile = new File(TestHelper.getTestDataDirectory(), "schema/gdp_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(schemaFile)) {
            schema = Schema.fromJson (fis, false);
        }
        Table table = Table.fromSource(f, TestHelper.getTestDataDirectory(), schema, DataSourceFormat.getDefaultCsvFormat());
        Iterator iter = table.iterator(true, false, true, false);
        Object obj = null;
        int cnt = 0;
        while (iter.hasNext()) {
            obj = iter.next();
            cnt++;
            if (cnt == 11086) {
                break;
            }
        }
        Object valueObj = ((Map)obj).get("Value");
        Assertions.assertTrue(valueObj instanceof BigInteger);
        BigInteger val = (BigInteger)valueObj;
        Assertions.assertEquals(18624475000000L, val.longValue());
    }
}
