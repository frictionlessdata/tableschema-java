package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class FieldConstraintsTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testRequiredTrue() throws Exception{

        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_REQUIRED, true);

        StringField field = new StringField("test",  null, null, null, null, constraints, null);

        String valueNotNull = field.castValue("This is a string value");
        violatedConstraints = field.checkConstraintViolations(valueNotNull);
        Assert.assertTrue(violatedConstraints.isEmpty());

        String valueNullConstraintNotEnforce = field.castValue(null, false, null);
        violatedConstraints = field.checkConstraintViolations(valueNullConstraintNotEnforce);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_REQUIRED));
        Assert.assertTrue((boolean)violatedConstraints.get(Field.CONSTRAINT_KEY_REQUIRED));

        Assert.assertNull(field.castValue(null));
    }

    @Test
    public void testRequiredFalse() throws Exception{

        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_REQUIRED, false);

        StringField field = new StringField("test",  null, null, null, null, constraints, null);

        String valueNotNull = field.castValue("This is a string value");
        violatedConstraints = field.checkConstraintViolations(valueNotNull);
        Assert.assertTrue(violatedConstraints.isEmpty());

        String valueNull = field.castValue(null);
        violatedConstraints = field.checkConstraintViolations(valueNull);
        Assert.assertTrue(violatedConstraints.isEmpty());
    }

    @Test
    public void testMinAndMaxLengthString() throws Exception{
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
        constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);

        StringField field = new StringField("test",  null, null, null, null, constraints, null);

        // 40 characters
        String valueLength40 = field.castValue("This string length is between 36 and 45.");
        violatedConstraints = field.checkConstraintViolations(valueLength40);
        Assert.assertTrue(violatedConstraints.isEmpty());

        // 36 characters
        String valueLength36 = field.castValue("This string length is 36 characters.");
        violatedConstraints = field.checkConstraintViolations(valueLength36);
        Assert.assertTrue(violatedConstraints.isEmpty());

        // 45 characters
        String valueLength45= field.castValue("This string length is precisely 45 char long.");
        violatedConstraints = field.checkConstraintViolations(valueLength45);
        Assert.assertTrue(violatedConstraints.isEmpty());

        // 35 characters
        String valueLength35 = field.castValue("This string length is less than 36.", false, null);
        violatedConstraints = field.checkConstraintViolations(valueLength35);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MIN_LENGTH));

        // 49 characters
        String valueLength49 = field.castValue("This string length is greater than 45 characters.", false, null);
        violatedConstraints = field.checkConstraintViolations(valueLength49);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAX_LENGTH));

        exception.expect(ConstraintsException.class);
        field.castValue("This string length is greater than 45 characters.");


    }

    @Test
    public void testMinandMaxLengthJSONObject() throws Exception{
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 2);
        constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 5);

        ObjectField field = new ObjectField("test", null, null, null, null, constraints, null);
        
        Map<String, Object> obj = new HashMap<>();

        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MIN_LENGTH));

        obj.put("one", 1);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MIN_LENGTH));

        obj.put("two", 2);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.put("three", 3);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.put("four", 4);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.put("five", 5);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.put("six", 6);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAX_LENGTH));

    }

    @Test
    public void testMinAndMaxLengthJSONArray(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 2);
        constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 5);

        ArrayField field = new ArrayField("test", Field.FIELD_FORMAT_DEFAULT, "title", null, null, constraints, null);

        List<Integer> obj = new ArrayList<>();
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MIN_LENGTH));

        obj.add(1);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MIN_LENGTH));

        obj.add(2);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.add(3);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.add(4);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.add(5);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.isEmpty());

        obj.add(6);
        violatedConstraints = field.checkConstraintViolations(createJsonNode(obj));
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAX_LENGTH));
    }

    @Test
    public void testMinimumAndMaximumInteger(){

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 2);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 5);

        IntegerField field = new IntegerField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null, constraints, null);

        for(int i=0; i < 7; i++){
            Map<String, Object> violatedConstraints = field.checkConstraintViolations(i);

            if(i >= 2 && i <=5){
                Assert.assertTrue(violatedConstraints.isEmpty());

            }else if(i < 2){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

            }else if(i > 5){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
            }
        }
    }

    public void testMinimumAndMaximumNumber(){
        //TODO: Implement
    }

    @Test
    public void testMinimumAndMaximumDate(){
        Map<String, Object> violatedConstraints = null;

        final String DATE_STRING_MINIMUM = "2000-01-15";
        final String DATE_STRING_MAXIMUM = "2019-01-15";

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dateMin = formatter.parseDateTime(DATE_STRING_MINIMUM);
        DateTime dateMax = formatter.parseDateTime(DATE_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, dateMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, dateMax);

        DateField field = new DateField("test",  null, null, null, null, constraints, null);

        DateTime datetime2017 = formatter.parseDateTime("2017-01-15");
        violatedConstraints = field.checkConstraintViolations(datetime2017);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime dateEqualMin = formatter.parseDateTime(DATE_STRING_MINIMUM);
        violatedConstraints = field.checkConstraintViolations(dateEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime dateEqualMax = formatter.parseDateTime(DATE_STRING_MAXIMUM);
        violatedConstraints = field.checkConstraintViolations(dateEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime dateLesserThanMinBy1Day = formatter.parseDateTime("2000-01-14");
        violatedConstraints = field.checkConstraintViolations(dateLesserThanMinBy1Day);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime dateLesserThanMinBy1Month = formatter.parseDateTime("1999-12-15");
        violatedConstraints = field.checkConstraintViolations(dateLesserThanMinBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime dateLesserThanMinBy1Year = formatter.parseDateTime("1999-01-15");
        violatedConstraints = field.checkConstraintViolations(dateLesserThanMinBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime dateGreaterThanMaxBy1Day = formatter.parseDateTime("2019-01-16");
        violatedConstraints = field.checkConstraintViolations(dateGreaterThanMaxBy1Day);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        DateTime dateGreaterThanMaxBy1Month = formatter.parseDateTime("2019-02-15");
        violatedConstraints = field.checkConstraintViolations(dateGreaterThanMaxBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        DateTime dateGreaterThanMaxBy1Year = formatter.parseDateTime("2020-01-15");
        violatedConstraints = field.checkConstraintViolations(dateGreaterThanMaxBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
    }

    @Test
    public void testMinimumAndMaximumTime(){

        Map<String, Object> violatedConstraints = null;

        final String TIME_STRING_MINIMUM = "11:05:12";
        final String TIME_STRING_MAXIMUM = "14:22:33";

        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
        DateTime timeMin = formatter.parseDateTime(TIME_STRING_MINIMUM);
        DateTime timeMax = formatter.parseDateTime(TIME_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, timeMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, timeMax);

        TimeField field = new TimeField("test", null, null, null, null, constraints, null);

        DateTime time = formatter.parseDateTime("13:00:05");
        violatedConstraints = field.checkConstraintViolations(time);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime timeEqualMin = formatter.parseDateTime(TIME_STRING_MINIMUM);
        violatedConstraints = field.checkConstraintViolations(timeEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime timeEqualMax = formatter.parseDateTime(TIME_STRING_MAXIMUM);
        violatedConstraints = field.checkConstraintViolations(timeEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime timeLesserThanMinBy1Sec = formatter.parseDateTime("11:05:11");
        violatedConstraints = field.checkConstraintViolations(timeLesserThanMinBy1Sec);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime timeLesserThanMinBy1Min = formatter.parseDateTime("11:04:12");
        violatedConstraints = field.checkConstraintViolations(timeLesserThanMinBy1Min);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime timeLesserThanMinBy1Hour = formatter.parseDateTime("10:05:12");
        violatedConstraints = field.checkConstraintViolations(timeLesserThanMinBy1Hour);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime timeGreaterThanMaxBy1Sec = formatter.parseDateTime("14:22:34");
        violatedConstraints = field.checkConstraintViolations(timeGreaterThanMaxBy1Sec);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        DateTime timeGreaterThanMaxBy1Min = formatter.parseDateTime("14:23:33");
        violatedConstraints = field.checkConstraintViolations(timeGreaterThanMaxBy1Min);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        DateTime timeGreaterThanMaxBy1Hour = formatter.parseDateTime("15:22:33");
        violatedConstraints = field.checkConstraintViolations(timeGreaterThanMaxBy1Hour);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

    }

    @Test
    public void testMinimumAndMaximumDatetime(){
        Map<String, Object> violatedConstraints = null;

        final String DATETIME_STRING_MINIMUM = "2000-01-15T13:44:33.000Z";
        final String DATETIME_STRING_MAXIMUM = "2019-01-15T13:44:33.000Z";

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime datetimeMin = formatter.parseDateTime(DATETIME_STRING_MINIMUM);
        DateTime datetimeMax = formatter.parseDateTime(DATETIME_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, datetimeMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, datetimeMax);

        Field field = new DatetimeField("test", null, null, null, null, constraints, null);

        DateTime datetime2017 = formatter.parseDateTime("2017-01-15T13:44:33.000Z");
        violatedConstraints = field.checkConstraintViolations(datetime2017);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime datetimeEqualMin = formatter.parseDateTime(DATETIME_STRING_MINIMUM);
        violatedConstraints = field.checkConstraintViolations(datetimeEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime datetimeEqualMax = formatter.parseDateTime(DATETIME_STRING_MAXIMUM);
        violatedConstraints = field.checkConstraintViolations(datetimeEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime datetimeLesserThanMinBy1Sec = formatter.parseDateTime("2000-01-15T13:44:32.000Z");
        violatedConstraints = field.checkConstraintViolations(datetimeLesserThanMinBy1Sec);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime datetimeGreaterThanMaxBy1Day = formatter.parseDateTime("2019-01-16T13:44:33.000Z");
        violatedConstraints = field.checkConstraintViolations(datetimeGreaterThanMaxBy1Day);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
    }

    @Test
    public void testMinimumAndMaximumYear(){
        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 1999);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 2018);

        YearField field = new YearField("test",  "default", "title", "desc", null, constraints, null);

        for(int i=1990; i < 2020; i++){
            Map<String, Object> violatedConstraints = field.checkConstraintViolations(i);

            if(i >= 1999 && i <=2018){
                Assert.assertTrue(violatedConstraints.isEmpty());

            }else if(i < 1999){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

            }else if(i > 2018){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
            }
        }

    }

    @Test
    public void testMinimumAndMaximumYearmonth(){
        Map<String, Object> violatedConstraints = null;

        final String YEARMONTH_STRING_MINIMUM = "2000-02";
        final String YEARMONTH_STRING_MAXIMUM = "2009-02";

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM");
        DateTime yearmonthMin = formatter.parseDateTime(YEARMONTH_STRING_MINIMUM);
        DateTime yearmonthMax = formatter.parseDateTime(YEARMONTH_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, yearmonthMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, yearmonthMax);

        Field field = new YearmonthField("test", null, null, null, null, constraints, null);

        DateTime yearmonth = formatter.parseDateTime("2005-05");
        violatedConstraints = field.checkConstraintViolations(yearmonth);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime yearmonthEqualMin = formatter.parseDateTime(YEARMONTH_STRING_MINIMUM);
        violatedConstraints = field.checkConstraintViolations(yearmonthEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime yearmonthEqualMax = formatter.parseDateTime(YEARMONTH_STRING_MAXIMUM);
        violatedConstraints = field.checkConstraintViolations(yearmonthEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime yearmonthLesserThanMinBy1Month = formatter.parseDateTime("2000-01");
        violatedConstraints = field.checkConstraintViolations(yearmonthLesserThanMinBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime yearmonthLesserThanMinBy1Year = formatter.parseDateTime("1999-02");
        violatedConstraints = field.checkConstraintViolations(yearmonthLesserThanMinBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        DateTime datetimeGreaterThanMaxBy1Month = formatter.parseDateTime("2009-03");
        violatedConstraints = field.checkConstraintViolations(datetimeGreaterThanMaxBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        DateTime datetimeGreaterThanMaxBy1Year = formatter.parseDateTime("2010-02");
        violatedConstraints = field.checkConstraintViolations(datetimeGreaterThanMaxBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

    }

    @Test
    public void testMinimumAndMaximumDuration(){
        Map<String, Object> violatedConstraints = null;

        final String DURATION_STRING_MINIMUM = "P2DT3H4M";
        final String DURATION_STRING_MAXIMUM = "P2DT5H4M";

        Duration durationMin = Duration.parse(DURATION_STRING_MINIMUM);
        Duration durationMax = Duration.parse(DURATION_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, durationMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, durationMax);

        Field field = new DurationField("test", null, null, null, null, constraints, null);

        Duration duration = Duration.parse("P2DT4H4M");
        violatedConstraints = field.checkConstraintViolations(duration);
        Assert.assertTrue(violatedConstraints.isEmpty());

        Duration durationEqualMin = Duration.parse(DURATION_STRING_MINIMUM);
        violatedConstraints = field.checkConstraintViolations(durationEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        Duration durationEqualMax = Duration.parse(DURATION_STRING_MAXIMUM);
        violatedConstraints = field.checkConstraintViolations(durationEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        Duration durationLesserThanMinBy1Min = Duration.parse("P2DT3H3M");
        violatedConstraints = field.checkConstraintViolations(durationLesserThanMinBy1Min);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        Duration durationGreaterThanMaxBy1Min = Duration.parse("P2DT5H5M");
        violatedConstraints = field.checkConstraintViolations(durationGreaterThanMaxBy1Min);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
    }

    @Test
    public void testPattern(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        constraints.put(Field.CONSTRAINT_KEY_PATTERN, "testing[0-9]+");

        Field field = new StringField("test", null, null, null, null, constraints, null);

        for(int i=0; i<12; i++){
            violatedConstraints = field.checkConstraintViolations("testing" + i);
            Assert.assertTrue(violatedConstraints.isEmpty());
        }

        violatedConstraints = field.checkConstraintViolations("testing");
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_PATTERN));
    }

    @Test
    public void testEnumString(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();

        List<String> enumStrings = new ArrayList();
        enumStrings.add("one");
        enumStrings.add("two");
        enumStrings.add("four");

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumStrings);

        Field field = new StringField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations("one");
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations("two");
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations("three");
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));

        violatedConstraints = field.checkConstraintViolations("four");
        Assert.assertTrue(violatedConstraints.isEmpty());
    }

    @Test
    public void testEnumInteger(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();

        List<Integer> enumInts = new ArrayList();
        enumInts.add(1);
        enumInts.add(2);
        enumInts.add(4);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumInts);

        IntegerField field = new IntegerField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));

        violatedConstraints = field.checkConstraintViolations(4);
        Assert.assertTrue(violatedConstraints.isEmpty());
    }

    @Test
    public void testEnumObject(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        List<JsonNode> enumObjs = new ArrayList();

        JsonNode obj1 = JsonUtil.getInstance().createNode("{\"one\": 1}");
        enumObjs.add(obj1);

        JsonNode obj2 = JsonUtil.getInstance().createNode("{\"one\": 1, \"two\": 2}");
        enumObjs.add(obj2);

        JsonNode obj3 = JsonUtil.getInstance().createNode("{\"one\": 1, \"two\": 2, \"four\": 4}");
        enumObjs.add(obj3);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumObjs);
        Field field = new ObjectField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(obj1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(obj2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(obj3);
        Assert.assertTrue(violatedConstraints.isEmpty());

        JsonNode obj4 = JsonUtil.getInstance().createNode("{\"one\": 1, \"two\": 2, \"three\": 3, \"four\": 4}");
        violatedConstraints = field.checkConstraintViolations(obj4);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }

    @Test
    public void testEnumArray(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        List<JsonNode> enumArrs = new ArrayList();

        ArrayNode arr1 = JsonUtil.getInstance().createArrayNode("[\"one\"]");
        enumArrs.add(arr1);

        ArrayNode arr2 = JsonUtil.getInstance().createArrayNode("[\"one\", \"2.6\"]");
        enumArrs.add(arr2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumArrs);
        Field field = new ArrayField("test",  null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(arr1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(arr2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        ArrayNode arr3 = JsonUtil.getInstance().createArrayNode("[\"one\", \"2.6\", \"3\"]");
        violatedConstraints = field.checkConstraintViolations(arr3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));

    }

    @Test
    public void testEnumDuration() throws Exception{
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        List<Duration> enumDurations = new ArrayList();

        Duration duration1 = Duration.parse("P2DT3H4M");
        enumDurations.add(duration1);

        Duration duration2 = Duration.parse("P3DT3H4M");
        enumDurations.add(duration2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDurations);
        Field field = new DurationField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(duration1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(duration2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        Duration duration3 = Duration.parse("P3DT3H5M");
        violatedConstraints = field.checkConstraintViolations(duration3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }

    @Test
    public void testEnumDatetime(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap();
        List<DateTime> enumDatetimes = new ArrayList();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        DateTime datetime1 = formatter.parseDateTime("2000-01-15T13:44:33.000Z");
        enumDatetimes.add(datetime1);

        DateTime datetime2 = formatter.parseDateTime("2019-01-15T13:44:33.000Z");
        enumDatetimes.add(datetime2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDatetimes);
        Field field = new DatetimeField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(datetime1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(datetime2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        DateTime datetime3 = formatter.parseDateTime("2003-01-15T13:44:33.000Z");
        violatedConstraints = field.checkConstraintViolations(datetime3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }
    
    private JsonNode createJsonNode(Object obj) {
    	return JsonUtil.getInstance().createNode(obj);
    }
}
