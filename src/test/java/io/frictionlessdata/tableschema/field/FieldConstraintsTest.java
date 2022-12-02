package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, Object> constraints = new HashMap<>();
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

        Map<String, Object> constraints = new HashMap<>();
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

        Map<String, Object> constraints = new HashMap<>();
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

        Map<String, Object> constraints = new HashMap<>();
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

        Map<String, Object> constraints = new HashMap<>();
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
    public void testMinimumAndMaximumBigInteger(){
        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 2);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 5);

        IntegerField field = new IntegerField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null, constraints, null);

        for(int i=0; i < 7; i++){
            Map<String, Object> violatedConstraints = field.checkConstraintViolations(BigInteger.valueOf(i));
            if(i >= 2 && i <=5){
                Assert.assertTrue(violatedConstraints.isEmpty());
            }else if(i < 2){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));
            }else {
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
            }
        }
    }

    @Test
    public void testMinimumAndMaximumNumber(){
        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 2.0);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 5.0);

        NumberField field = new NumberField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null, constraints, null);

        for(int i=0; i < 7; i++){
            Map<String, Object> violatedConstraints = field.checkConstraintViolations(i);

            if(i >= 2 && i <=5){
                Assert.assertTrue(violatedConstraints.isEmpty());
            }else if(i < 2){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));
            }else {
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
            }
        }
    }

    @Test
    public void testMinimumAndMaximumDate(){
        Map<String, Object> violatedConstraints = null;

        final String DATE_STRING_MINIMUM = "2000-01-15";
        final String DATE_STRING_MAXIMUM = "2019-01-15";

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateMin = LocalDate.parse(DATE_STRING_MINIMUM, formatter);
        LocalDate dateMax = LocalDate.parse(DATE_STRING_MAXIMUM, formatter);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, dateMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, dateMax);

        DateField field = new DateField("test",  null, null, null, null, constraints, null);

        LocalDate datetime2017 = LocalDate.parse("2017-01-15", formatter);
        violatedConstraints = field.checkConstraintViolations(datetime2017);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalDate dateEqualMin = LocalDate.parse(DATE_STRING_MINIMUM, formatter);
        violatedConstraints = field.checkConstraintViolations(dateEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalDate dateEqualMax = LocalDate.parse(DATE_STRING_MAXIMUM, formatter);
        violatedConstraints = field.checkConstraintViolations(dateEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalDate dateLesserThanMinBy1Day = LocalDate.parse("2000-01-14", formatter);
        violatedConstraints = field.checkConstraintViolations(dateLesserThanMinBy1Day);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        LocalDate dateLesserThanMinBy1Month = LocalDate.parse("1999-12-15", formatter);
        violatedConstraints = field.checkConstraintViolations(dateLesserThanMinBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        LocalDate dateLesserThanMinBy1Year = LocalDate.parse("1999-01-15", formatter);
        violatedConstraints = field.checkConstraintViolations(dateLesserThanMinBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        LocalDate dateGreaterThanMaxBy1Day = LocalDate.parse("2019-01-16", formatter);
        violatedConstraints = field.checkConstraintViolations(dateGreaterThanMaxBy1Day);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        LocalDate dateGreaterThanMaxBy1Month = LocalDate.parse("2019-02-15", formatter);
        violatedConstraints = field.checkConstraintViolations(dateGreaterThanMaxBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        LocalDate dateGreaterThanMaxBy1Year = LocalDate.parse("2020-01-15", formatter);
        violatedConstraints = field.checkConstraintViolations(dateGreaterThanMaxBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
    }

    @Test
    public void testMinimumAndMaximumTime(){

        Map<String, Object> violatedConstraints = null;

        final String TIME_STRING_MINIMUM = "11:05:12";
        final String TIME_STRING_MAXIMUM = "14:22:33";

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime timeMin = LocalTime.parse(TIME_STRING_MINIMUM, formatter);
        LocalTime timeMax = LocalTime.parse(TIME_STRING_MAXIMUM, formatter);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, timeMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, timeMax);

        TimeField field = new TimeField("test", null, null, null, null, constraints, null);

        LocalTime time = LocalTime.parse("13:00:05", formatter);
        violatedConstraints = field.checkConstraintViolations(time);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalTime timeEqualMin = LocalTime.parse(TIME_STRING_MINIMUM, formatter);
        violatedConstraints = field.checkConstraintViolations(timeEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalTime timeEqualMax = LocalTime.parse(TIME_STRING_MAXIMUM, formatter);
        violatedConstraints = field.checkConstraintViolations(timeEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalTime timeLesserThanMinBy1Sec = LocalTime.parse("11:05:11", formatter);
        violatedConstraints = field.checkConstraintViolations(timeLesserThanMinBy1Sec);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        LocalTime timeLesserThanMinBy1Min = LocalTime.parse("11:04:12", formatter);
        violatedConstraints = field.checkConstraintViolations(timeLesserThanMinBy1Min);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        LocalTime timeLesserThanMinBy1Hour = LocalTime.parse("10:05:12", formatter);
        violatedConstraints = field.checkConstraintViolations(timeLesserThanMinBy1Hour);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        LocalTime timeGreaterThanMaxBy1Sec = LocalTime.parse("14:22:34", formatter);
        violatedConstraints = field.checkConstraintViolations(timeGreaterThanMaxBy1Sec);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        LocalTime timeGreaterThanMaxBy1Min = LocalTime.parse("14:23:33", formatter);
        violatedConstraints = field.checkConstraintViolations(timeGreaterThanMaxBy1Min);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        LocalTime timeGreaterThanMaxBy1Hour = LocalTime.parse("15:22:33", formatter);
        violatedConstraints = field.checkConstraintViolations(timeGreaterThanMaxBy1Hour);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

    }

    @Test
    public void testMinimumAndMaximumDatetime(){
        Map<String, Object> violatedConstraints = null;

        final String DATETIME_STRING_MINIMUM = "2000-01-15T13:44:33.000+0000";
        final String DATETIME_STRING_MAXIMUM = "2019-01-15T13:44:33.000+0000";

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        ZonedDateTime datetimeMin = ZonedDateTime.parse(DATETIME_STRING_MINIMUM, formatter);
        ZonedDateTime datetimeMax = ZonedDateTime.parse(DATETIME_STRING_MAXIMUM, formatter);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, datetimeMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, datetimeMax);

        DatetimeField field = new DatetimeField("test", null, null, null, null, constraints, null);

        ZonedDateTime datetime2017 = ZonedDateTime.parse("2017-01-15T13:44:33.000+0000", formatter);
        violatedConstraints = field.checkConstraintViolations(datetime2017);
        Assert.assertTrue(violatedConstraints.isEmpty());

        ZonedDateTime datetimeEqualMin = ZonedDateTime.parse(DATETIME_STRING_MINIMUM, formatter);
        violatedConstraints = field.checkConstraintViolations(datetimeEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        ZonedDateTime datetimeEqualMax = ZonedDateTime.parse(DATETIME_STRING_MAXIMUM, formatter);
        violatedConstraints = field.checkConstraintViolations(datetimeEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        ZonedDateTime datetimeLesserThanMinBy1Sec = ZonedDateTime.parse("2000-01-15T13:44:32.000+0000", formatter);
        violatedConstraints = field.checkConstraintViolations(datetimeLesserThanMinBy1Sec);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        ZonedDateTime datetimeGreaterThanMaxBy1Day = ZonedDateTime.parse("2019-01-16T13:44:33.000+0000", formatter);
        violatedConstraints = field.checkConstraintViolations(datetimeGreaterThanMaxBy1Day);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
    }

    @Test
    public void testMinimumAndMaximumYear(){
        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 1999);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 2018);

        YearField field = new YearField("test",  "default", "title", "desc", null, constraints, null);

        for(int i=1990; i < 2020; i++){
            Map<String, Object> violatedConstraints = field.checkConstraintViolations(Year.of(i));

            if(i >= 1999 && i <=2018){
                Assert.assertTrue(violatedConstraints.isEmpty());

            }else if(i < 1999){
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

            }else {
                Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));
            }
        }

    }

    @Test
    public void testMinimumAndMaximumYearmonth(){
        Map<String, Object> violatedConstraints = null;

        final String YEARMONTH_STRING_MINIMUM = "2000-02";
        final String YEARMONTH_STRING_MAXIMUM = "2009-02";

        YearMonth yearmonthMin = YearMonth.parse(YEARMONTH_STRING_MINIMUM);
        YearMonth yearmonthMax = YearMonth.parse(YEARMONTH_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, yearmonthMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, yearmonthMax);

        YearmonthField field = new YearmonthField("test", null, null, null, null, constraints, null);

        YearMonth yearmonth = YearMonth.parse("2005-05");
        violatedConstraints = field.checkConstraintViolations(yearmonth);
        Assert.assertTrue(violatedConstraints.isEmpty());

        YearMonth yearmonthEqualMin = YearMonth.parse(YEARMONTH_STRING_MINIMUM);
        violatedConstraints = field.checkConstraintViolations(yearmonthEqualMin);
        Assert.assertTrue(violatedConstraints.isEmpty());

        YearMonth yearmonthEqualMax = YearMonth.parse(YEARMONTH_STRING_MAXIMUM);
        violatedConstraints = field.checkConstraintViolations(yearmonthEqualMax);
        Assert.assertTrue(violatedConstraints.isEmpty());

        YearMonth yearmonthLesserThanMinBy1Month = YearMonth.parse("2000-01");
        violatedConstraints = field.checkConstraintViolations(yearmonthLesserThanMinBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        YearMonth yearmonthLesserThanMinBy1Year = YearMonth.parse("1999-02");
        violatedConstraints = field.checkConstraintViolations(yearmonthLesserThanMinBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MINIMUM));

        YearMonth YearMonthGreaterThanMaxBy1Month = YearMonth.parse("2009-03");
        violatedConstraints = field.checkConstraintViolations(YearMonthGreaterThanMaxBy1Month);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

        YearMonth YearMonthGreaterThanMaxBy1Year = YearMonth.parse("2010-02");
        violatedConstraints = field.checkConstraintViolations(YearMonthGreaterThanMaxBy1Year);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_MAXIMUM));

    }

    @Test
    public void testMinimumAndMaximumDuration(){
        Map<String, Object> violatedConstraints = null;

        final String DURATION_STRING_MINIMUM = "P2DT3H4M";
        final String DURATION_STRING_MAXIMUM = "P2DT5H4M";

        Duration durationMin = Duration.parse(DURATION_STRING_MINIMUM);
        Duration durationMax = Duration.parse(DURATION_STRING_MAXIMUM);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_MINIMUM, durationMin);
        constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, durationMax);

        DurationField field = new DurationField("test", null, null, null, null, constraints, null);

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

        Map<String, Object> constraints = new HashMap<>();
        constraints.put(Field.CONSTRAINT_KEY_PATTERN, "testing[0-9]+");

        StringField field = new StringField("test", null, null, null, null, constraints, null);

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

        Map<String, Object> constraints = new HashMap<>();

        List<String> enumStrings = new ArrayList<>();
        enumStrings.add("one");
        enumStrings.add("two");
        enumStrings.add("four");

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumStrings);

        StringField field = new StringField("test", null, null, null, null, constraints, null);

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

        Map<String, Object> constraints = new HashMap<>();

        List<Integer> enumInts = new ArrayList<>();
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

        Map<String, Object> constraints = new HashMap<>();
        List<JsonNode> enumObjs = new ArrayList<>();

        JsonNode obj1 = JsonUtil.getInstance().createNode("{\"one\": 1}");
        enumObjs.add(obj1);

        JsonNode obj2 = JsonUtil.getInstance().createNode("{\"one\": 1, \"two\": 2}");
        enumObjs.add(obj2);

        JsonNode obj3 = JsonUtil.getInstance().createNode("{\"one\": 1, \"two\": 2, \"four\": 4}");
        enumObjs.add(obj3);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumObjs);
        ObjectField field = new ObjectField("test", null, null, null, null, constraints, null);

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

        Map<String, Object> constraints = new HashMap<>();
        List<JsonNode> enumArrs = new ArrayList<>();

        ArrayNode arr1 = JsonUtil.getInstance().createArrayNode("[\"one\"]");
        enumArrs.add(arr1);

        ArrayNode arr2 = JsonUtil.getInstance().createArrayNode("[\"one\", \"2.6\"]");
        enumArrs.add(arr2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumArrs);
        ArrayField field = new ArrayField("test",  null, null, null, null, constraints, null);

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

        Map<String, Object> constraints = new HashMap<>();
        List<Duration> enumDurations = new ArrayList<>();

        Duration duration1 = Duration.parse("P2DT3H4M");
        enumDurations.add(duration1);

        Duration duration2 = Duration.parse("P3DT3H4M");
        enumDurations.add(duration2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDurations);
        DurationField field = new DurationField("test", null, null, null, null, constraints, null);

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

        Map<String, Object> constraints = new HashMap<>();
        List<ZonedDateTime> enumDatetimes = new ArrayList<>();

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        ZonedDateTime datetime1 = ZonedDateTime.parse("2000-01-15T13:44:33.000+0000", formatter);

        enumDatetimes.add(datetime1);

        ZonedDateTime datetime2 = ZonedDateTime.parse("2019-01-15T13:44:33.000+0000", formatter);
        enumDatetimes.add(datetime2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDatetimes);
        DatetimeField field = new DatetimeField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(datetime1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(datetime2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        ZonedDateTime datetime3 = ZonedDateTime.parse("2003-01-15T13:44:33.000+0000", formatter);
        violatedConstraints = field.checkConstraintViolations(datetime3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }

    @Test
    public void testEnumLocalTime(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap<>();
        List<LocalTime> enumTimes = new ArrayList<>();

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime time1 = LocalTime.parse("13:44:33", formatter);

        enumTimes.add(time1);

        LocalTime time2 = LocalTime.parse("00:44:33", formatter);
        enumTimes.add(time2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumTimes);
        TimeField field = new TimeField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(time1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(time2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalTime datetime3 = LocalTime.parse("12:44:33", formatter);
        violatedConstraints = field.checkConstraintViolations(datetime3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }

    @Test
    public void testEnumLocalDate(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap<>();
        List<LocalDate> enumDates = new ArrayList<>();

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date1 = LocalDate.parse("2000-01-15", formatter);

        enumDates.add(date1);

        LocalDate date2 = LocalDate.parse("2019-01-15", formatter);
        enumDates.add(date2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDates);
        DateField field = new DateField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(date1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(date2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        LocalDate date3 = LocalDate.parse("2003-01-15", formatter);
        violatedConstraints = field.checkConstraintViolations(date3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }

    @Test
    public void testEnumYear(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap<>();
        List<Year> enumDates = new ArrayList<>();

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy");
        Year date1 = Year.parse("2000", formatter);

        enumDates.add(date1);

        Year date2 = Year.parse("2019", formatter);
        enumDates.add(date2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDates);
        YearField field = new YearField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(date1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(date2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        Year date3 = Year.parse("2003", formatter);
        violatedConstraints = field.checkConstraintViolations(date3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }

    @Test
    public void testEnumYearMonth(){
        Map<String, Object> violatedConstraints = null;

        Map<String, Object> constraints = new HashMap<>();
        List<YearMonth> enumDates = new ArrayList<>();

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth date1 = YearMonth.parse("2000-01", formatter);

        enumDates.add(date1);

        YearMonth date2 = YearMonth.parse("2019-01", formatter);
        enumDates.add(date2);

        constraints.put(Field.CONSTRAINT_KEY_ENUM, enumDates);
        YearmonthField field = new YearmonthField("test", null, null, null, null, constraints, null);

        violatedConstraints = field.checkConstraintViolations(date1);
        Assert.assertTrue(violatedConstraints.isEmpty());

        violatedConstraints = field.checkConstraintViolations(date2);
        Assert.assertTrue(violatedConstraints.isEmpty());

        YearMonth date3 = YearMonth.parse("2003-01", formatter);
        violatedConstraints = field.checkConstraintViolations(date3);
        Assert.assertTrue(violatedConstraints.containsKey(Field.CONSTRAINT_KEY_ENUM));
    }


    private JsonNode createJsonNode(Object obj) {
    	return JsonUtil.getInstance().createNode(obj);
    }
}
