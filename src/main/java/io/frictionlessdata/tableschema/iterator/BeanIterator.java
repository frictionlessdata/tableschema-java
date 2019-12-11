package io.frictionlessdata.tableschema.iterator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.field.ObjectField;
import io.frictionlessdata.tableschema.field.ReflectionUtils;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.field.Field;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BeanIterator<T> extends TableIterator<T> {
    private Class<T> type = null;
    private CsvMapper mapper = new CsvMapper();
    private CsvSchema csvSchema;

    public BeanIterator(Table table,  Class<T> beanType) throws Exception {
        super(table);
        this.type = beanType;
        csvSchema = mapper.typedSchemaFor(beanType);
    }

    public BeanIterator(Table table,  Class<T> beanType, boolean relations) throws Exception {
        this(table, beanType);
        super.relations = relations;
    }

    @Override
    public T next() {
        T retVal;
        String[] row = super.wrappedIterator.next();

        Map<String, AnnotatedField> fields = new HashMap<>();
        Map<String, String> fieldNames = ReflectionUtils.getFieldNameMapping(mapper, type);
        CsvMapper mapper = new CsvMapper();
        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        JavaType jType = mapper.constructType(type);
        BeanDescription desc = mapper.getSerializationConfig()
                .introspect(jType);
        List<BeanPropertyDefinition> properties = desc.findProperties();
        for (BeanPropertyDefinition def : properties) {
            AnnotatedField field = def.getField();
            String declaredName = def.getName();
            fields.put(declaredName, field);
        }

        if (this.schema != null) {
            try {
                retVal = type.newInstance();

                for (int i = 0; i < row.length; i++) {
                    Field field = this.schema.getFields().get(i);
                    AnnotatedField aF = fields.get(field.getName());
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
                        Coordinate coordinate = new Coordinate((double) arr[0], (double)arr[1]);
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
        } else {
            throw new TableSchemaException("Cannot use a BeanIterator without Schema");
        }
        //return null;
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
