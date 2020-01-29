package io.frictionlessdata.tableschema.iterator;

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.field.ObjectField;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.field.Field;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    private Class<T> type = null;
    private CsvMapper mapper = new CsvMapper();

    public BeanIterator(Table table,  Class<T> beanType, boolean relations) throws Exception {
        this.type = beanType;
        this.relations = relations;
        init(table);
    }

    /**
     * Overrides {@link TableIterator#init(Table)} and instead of copying the Schema from the Table,
     * infers a Schema from the Bean type.
     * @param table The Table to iterate data on
     * @throws Exception in case header parsing, Schema inferral or Schema validation fails
     */
    @Override
    void init(Table table) throws Exception{
        fieldOptions = table.getFieldOptions();
        mapping = table.getSchemaHeaderMapping();
        headers = table.getHeaders();
        schema = BeanSchema.infer(type);
        table.validate();
        wrappedIterator = table.getDataSourceFormat().iterator();
    }

    @Override
    public T next() {
        T retVal;
        final String[] row = super.wrappedIterator.next();

        try {
            retVal = type.newInstance();

            for (int i = 0; i < row.length; i++) {
                Field field = schema.getFields().get(i);
                AnnotatedField aF = ((BeanSchema) schema).getAnnotatedField(field.getName());
                // we may have a field that can have different formats
                // but the Schema doesn't know about the true format
                if (field.getFormat().equals(Field.FIELD_FORMAT_DEFAULT)) {
                    // have to parse format here when we have actual sample data
                    // instead of at BeanSchema inferral time
                    String fieldFormat = field.parseFormat(row[i], null);
                    field.setFormat(fieldFormat);
                }
                Object val = field.castValue(row[i], true, fieldOptions);
                Class annotatedFieldClass = aF.getRawType();
                aF.fixAccess(true);
                if (Number.class.isAssignableFrom(annotatedFieldClass)) {
                    setNumberField(retVal, aF, (Number)val);
                } else if (byte.class.equals(annotatedFieldClass)){
                    aF.setValue(retVal, new Byte(((BigInteger)val).shortValue()+""));
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
                } else if (Coordinate.class.isAssignableFrom(annotatedFieldClass)) {
                    double[] arr = (double[])val;
                    Coordinate coordinate = new Coordinate(arr[0], arr[1]);
                    aF.setValue(retVal, coordinate);
                } else if (field instanceof ObjectField){
                    if (annotatedFieldClass.equals(JSONObject.class)) {
                        aF.setValue(retVal, new JSONObject((String)val));
                    } else {
                        aF.setValue(retVal, val);
                    }
                } else {
                    aF.setValue(retVal, val);
                }
            }
            return retVal;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TableSchemaException(e);
        }

    }

    private void setNumberField(T obj, AnnotatedField field, Number val) {
        Class fClass = field.getRawType();
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
