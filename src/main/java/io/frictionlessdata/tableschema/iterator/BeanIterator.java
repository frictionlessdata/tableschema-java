package io.frictionlessdata.tableschema.iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.annotations.FieldFormat;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.field.ArrayField;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.field.ObjectField;
import io.frictionlessdata.tableschema.field.StringField;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.locationtech.jts.geom.Coordinate;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link TableIterator} based on a Java Bean class instead of a {@link io.frictionlessdata.tableschema.schema.Schema}.
 * It therefore disregards the Schema set on the {@link io.frictionlessdata.tableschema.Table} the iterator works
 * on but creates its own Schema from the supplied `beanType`.
 *
 * @param <T> the Bean class this BeanIterator expects
 */
public class BeanIterator<T> extends TableIterator<T> {
    private final Class<T> type;

    public BeanIterator(Table table,  Class<T> beanType, boolean relations)  {
        this.type = beanType;
        this.relations = relations;
        init(table);
    }

    /**
     * Overrides {@link TableIterator#init(Table)} and instead of copying the Schema from the Table,
     * infers a Schema from the Bean type.
     * @param table The Table to iterate data on
     */
    @Override
    void init(Table table){
        mapping = table.getSchemaHeaderMapping();
        headers = table.getHeaders();
        schema = BeanSchema.infer(type);
        table.validate();
        wrappedIterator = table.getTableDataSource().iterator();
    }

    @Override
    public T next() {
        T retVal;
        final String[] row = super.wrappedIterator.next();

        try {
            retVal = type.getDeclaredConstructor().newInstance();
            for (int i = 0; i < row.length; i++) {
                String fieldName = headers[i];
                Field<?> field = schema.getField(fieldName);
                if (null == field) {
                    continue;
                }
                AnnotatedField aF = ((BeanSchema) schema).getAnnotatedField(fieldName);
                FieldFormat annotation = aF.getAnnotation(FieldFormat.class);
                String fieldFormat = field.getFormat();
                if (null != annotation) {
                    fieldFormat = annotation.format();
                } else {
                    // we may have a field that can have different formats
                    // but the Schema doesn't know about the true format
                    if (fieldFormat.equals(Field.FIELD_FORMAT_DEFAULT)) {
                        // have to parse format here when we have actual sample data
                        // instead of at BeanSchema inferral time
                        fieldFormat = field.parseFormat(row[i], null);
                    }
                }
                field.setFormat(fieldFormat);
                Object val = field.castValue(row[i]);
                if (null == val)
                    continue;
                Class<?> annotatedFieldClass = aF.getRawType();
                aF.fixAccess(true);
                if (Number.class.isAssignableFrom(annotatedFieldClass)) {
                    setNumberField(retVal, aF, (Number)val);
                } else if (byte.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, Byte.valueOf(((BigInteger)val).shortValue()+""));
                } else if (short.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, ((BigInteger)val).shortValue());
                } else if (int.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, ((BigInteger)val).intValue());
                } else if (long.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, ((BigInteger)val).longValue());
                } else if (float.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, ((BigDecimal)val).floatValue());
                } else if (double.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, ((BigDecimal)val).doubleValue());
                } else if (UUID.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, UUID.fromString((String)val));
                } else if (LocalDate.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, val);
                } else if (LocalTime.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, val);
                } else if (Boolean.class.equals(annotatedFieldClass) || boolean.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, val);
                } else if (Coordinate.class.isAssignableFrom(annotatedFieldClass)) {
                    double[] arr = (double[])val;
                    Coordinate coordinate = new Coordinate(arr[0], arr[1]);
                    aF.setValue(retVal, coordinate);
                } else if (field instanceof ArrayField){
                    if (Collection.class.isAssignableFrom(annotatedFieldClass)) {
                        Object[] convVal = (Object[])val;
                        aF.setValue(retVal, Arrays.asList(convVal));
                    } else {
                        aF.setValue(retVal, JsonUtil.getInstance().convertValue(val, String[].class));
                    }
                } else if (field instanceof ObjectField){
                    if (annotatedFieldClass.equals(JsonNode.class)) {
                        aF.setValue(retVal, JsonUtil.getInstance().readValue(val.toString()));
                    } else {
                    	// this conversion method may also be used for the other field types
                        aF.setValue(retVal, JsonUtil.getInstance().convertValue(val, annotatedFieldClass));
                    }
                } else if (byte[].class.equals(annotatedFieldClass)){
                    byte[] bytes = Base64.getDecoder().decode(val.toString());
                    aF.setValue(retVal, bytes);
                } else if (field instanceof StringField){
                    aF.setValue(retVal, val.toString());
                } else {
                    aF.setValue(retVal, val);
                }
            }
            return retVal;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new TableSchemaException(e);
        }
    }

    private void setNumberField(T obj, AnnotatedField field, Number val) {
        Class<?> fClass = field.getRawType();
        if (fClass.equals(BigDecimal.class)) {
            BigDecimal big = new BigDecimal(val.toString());
            field.setValue(obj, big);
        } else if (fClass.equals(Float.class)) {
            field.setValue(obj, (val.floatValue()));
        } else if (fClass.equals(Double.class)) {
            field.setValue(obj, (val.doubleValue()));
        } else if (fClass.equals(Integer.class)) {
            field.setValue(obj, (val.intValue()));
        } else if (fClass.equals(Long.class)) {
            field.setValue(obj, (val.longValue()));
        } else if (fClass.equals(Short.class)) {
            field.setValue(obj, (val.shortValue()));
        } else if (fClass.equals(Byte.class)) {
            field.setValue(obj, (val.byteValue()));
        } else if (fClass.equals(BigInteger.class)) {
            BigInteger big = new BigInteger(val.toString());
            field.setValue(obj, big);
        } else if (fClass.equals(AtomicInteger.class)) {
            AtomicInteger ai = new AtomicInteger();
            ai.set(val.intValue());
            field.setValue(obj, ai);
        } else if (fClass.equals(AtomicLong.class)) {
            AtomicLong ai = new AtomicLong();
            ai.set(val.longValue());
            field.setValue(obj, ai);
        } else if (fClass.equals(AtomicDouble.class)) {
            AtomicDouble ai = new AtomicDouble();
            ai.set(val.doubleValue());
            field.setValue(obj, ai);
        }
    }
}
