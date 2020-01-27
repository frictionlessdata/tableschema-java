package io.frictionlessdata.tableschema.field;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DateFieldTest {

    private DateField dateField = new DateField();

    ArrayList<String> dates = Lists.newArrayList("1991-12-25", "1992-12-31", "1990-10-02", "1992-04-28","2020-02-29");

    @Test
    void parseValue() {
        dates.forEach(x->{
            LocalDate aDefault = dateField.parseValue(x, "default", null);
            System.out.println(aDefault);
        });
    }
}