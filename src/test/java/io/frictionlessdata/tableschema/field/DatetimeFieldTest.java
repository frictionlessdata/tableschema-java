package io.frictionlessdata.tableschema.field;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DatetimeFieldTest {

    @Test
    void parseValueNoT() {
        DatetimeField datetimeField = new DatetimeField();
        String dateAsString ="2016-04-05 13:23:05";
        ZonedDateTime aDefault = datetimeField.parseValue(dateAsString, "default", null);
        System.out.println(aDefault);
    }

    @Test
    void parseValue() {
        DatetimeField datetimeField = new DatetimeField();
        String dateAsString ="2016-04-05T13:23:05";
        ZonedDateTime aDefault = datetimeField.parseValue(dateAsString, "default", null);
        System.out.println(aDefault);
    }

    @Test
    void parseValue2() {
        DatetimeField datetimeField = new DatetimeField();
        String dateAsString ="2016-04-05T13:23:05Z";
        ZonedDateTime aDefault = datetimeField.parseValue(dateAsString, "default", null);
        System.out.println(aDefault);
    }


    @Test
    void parseValue3() {
        DatetimeField datetimeField = new DatetimeField();
        String dateAsString ="2016-04-05T13:23:05.000";
        ZonedDateTime aDefault = datetimeField.parseValue(dateAsString, "default", null);
        System.out.println(aDefault);
        //2016-04-05T13:23:05.000
    }

    @Test
    void parseValue4() {
        DatetimeField datetimeField = new DatetimeField();
        String dateAsString ="2016-04-05T13:23:05.000Z";
        ZonedDateTime aDefault = datetimeField.parseValue(dateAsString, "default", null);
        System.out.println(aDefault);
        //2016-04-05T13:23:05.000+0000
    }


}