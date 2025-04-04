package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.AtomicDouble;
import org.locationtech.jts.geom.Coordinate;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FieldInferrer {

    public static Field<?> infer(Object o) {
        if (null == o) {
            return new AnyField("Any");
        }
        Class<?> oClass = o.getClass();
        Field<?> f  = generateNumberField(oClass, oClass.getName());
        if (null != f)
            return f;
        f = generateStringField(oClass,  oClass.getName());
        if (null != f)
            return f;
        if (LocalDate.class.equals(oClass)){
            f = new DateField(oClass.getName());
        } else if (LocalTime.class.equals(oClass)){
            f = new TimeField(oClass.getName());
        } else if (Boolean.class.equals(oClass)){
            f = new BooleanField(oClass.getName());
        } else if (Coordinate.class.isAssignableFrom(oClass)) {
            f = new GeopointField(oClass.getName());
        } else if (Collection.class.isAssignableFrom(oClass)
                    || (o instanceof Array)) {
                f = new ArrayField(oClass.getName());
        }
        if (oClass.equals(JsonNode.class)) {
            f = new ObjectField(oClass.getName());
        } else if (oClass.equals(Object.class)) {
            f = new ObjectField(oClass.getName());
        }
        return f;
    }

    private static Field<?> generateStringField(Class<?> declaredClass, String name) {
        Field<?> f = null;
        if (UUID.class.equals(declaredClass)){
            f = new StringField(name);
            f.setFormat("uuid");
        } else if (byte[].class.equals(declaredClass)){
            f = new StringField(name);
            f.setFormat("binary");
        } else if (String.class.equals(declaredClass)) {
            f = new StringField(name);
        }
        return f;
    }

    private static Field<?> generateNumberField(Class<?> declaredClass, String name) {
        Field<?> field = null;
        if ((declaredClass.equals(Integer.class))
                || (declaredClass.equals(int.class))
                || (declaredClass.equals(Long.class))
                || (declaredClass.equals(long.class))
                || (declaredClass.equals(Short.class))
                || (declaredClass.equals(short.class))
                || (declaredClass.equals(Byte.class))
                || (declaredClass.equals(byte.class))
                || (declaredClass.equals(BigInteger.class))
                || (declaredClass.equals(AtomicInteger.class))
                || (declaredClass.equals(AtomicLong.class))) {
            field = new IntegerField(name);
        } else {
            if ((declaredClass.equals(Float.class))
                    || (declaredClass.equals(float.class))
                    || (declaredClass.equals(Double.class))
                    || (declaredClass.equals(double.class))
                    || (declaredClass.equals(BigDecimal.class))
                    || (declaredClass.equals(AtomicDouble.class))) {
                field = new NumberField(name);
            }
        }
        return field;
    }
}
