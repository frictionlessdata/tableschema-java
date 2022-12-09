package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.annotations.FieldFormat;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.util.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.DirectPosition2D;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.frictionlessdata.tableschema.util.ReflectionUtil.getBeanDescription;

public class BeanSchema extends Schema {

    @JsonIgnore
    Map<String, Field<?>> fieldMap;

    @JsonIgnore
    Map<String, AnnotatedField> annotatedFieldMap;

    @JsonIgnore
    private CsvSchema csvSchema;

    private BeanSchema (Class<?> beanClass) {
        _infer(beanClass);
    }

    public static BeanSchema infer(Class<?> beanClass) {
        return new BeanSchema(beanClass);
    }

    @JsonIgnore
    public String[] getHeaders() {
        List<String> retVal = new ArrayList<>();
        Iterator<CsvSchema.Column> iterator = csvSchema.iterator();
        iterator.forEachRemaining((c) -> {
            retVal.add(c.getName());
        });
        return retVal.toArray(new String[]{});
    }

    public Field<?> getField(String name) {
        return fieldMap.get(name);
    }


    public CsvSchema getCsvSchema() {
        return csvSchema;
    }

    public Map<String, AnnotatedField> getAnnotatedFieldMap() {
        return annotatedFieldMap;
    }


    public AnnotatedField getAnnotatedField(String name) {
        return annotatedFieldMap.get(name);
    }

    void setAnnotatedFieldMap(Map<String, AnnotatedField> fieldMap) {
        this.annotatedFieldMap = fieldMap;
    }

    private void _infer(Class<?> beanClass) {
        strictValidation = true;
        fields = new ArrayList<>();
        CsvMapper mapper = new CsvMapper();
        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        CsvSchema csvSchema = mapper.typedSchemaFor(beanClass);
        Iterator<CsvSchema.Column> iterator = csvSchema.iterator();
        Map<String, String> fieldNames = ReflectionUtil.getFieldNameMapping(beanClass);
        while (iterator.hasNext()) {
            CsvSchema.Column next = iterator.next();
            String name = next.getName();
            CsvSchema.ColumnType type = next.getType();
            Field<?> field;
            Class<?> declaredClass;
            String fieldMethodName = fieldNames.get(name);
            if (null == fieldMethodName) {
                continue;
            }
            try {
                java.lang.reflect.Field declaredField = beanClass.getDeclaredField(fieldMethodName);
                declaredClass = declaredField.getType();
            } catch (NoSuchFieldException ex) {
                continue;
            }
            switch (type) {
                case ARRAY:
                    field = new ArrayField(name);
                    break;
                case STRING: {
                    field = new StringField(name);
                    if (declaredClass.equals(byte[].class)) {
                        field.setFormat("binary");
                    }
                }
                break;
                case BOOLEAN:
                    field = new BooleanField(name);
                    break;
                case NUMBER: {
                    field = generateNumberField(declaredClass, name);
                }
                break;
                case NUMBER_OR_STRING: {
                    field = generateNumberField(declaredClass, name);
                    if (null == field) {
                        if (declaredClass.equals(Year.class))
                            field = new YearField(name);
                        else if (declaredClass.equals(YearMonth.class))
                            field = new YearmonthField(name);
                        else if (declaredClass.equals(LocalDate.class))
                            field = new DateField(name);
                        else if ((declaredClass.equals(ZonedDateTime.class))
                                || (declaredClass.equals(LocalDateTime.class))
                                || (declaredClass.equals(OffsetDateTime.class))
                                || (declaredClass.equals(Calendar.class))
                                || (declaredClass.equals(Date.class)))
                            field = new DatetimeField(name);
                        else if ((declaredClass.equals(Duration.class))
                                || (declaredClass.equals(Period.class)))
                            field = new DurationField(name);
                        else if ((declaredClass.equals(LocalTime.class))
                                || (declaredClass.equals(OffsetTime.class)))
                            field = new TimeField(name);
                        else if ((declaredClass.equals(Coordinate.class))
                                || (declaredClass.equals(DirectPosition2D.class)))
                            field = new GeopointField(name);
                        else if (declaredClass.equals(JsonNode.class))
                            field = new ObjectField(name);
                        else if (declaredClass.equals(Map.class))
                            field = new ObjectField(name);
                        else if (declaredClass.equals(UUID.class))
                            field = new StringField(name);
                    }
                }
                break;
                default:
                    field = new AnyField(name);
            }
            if (null == field) {
                String canonicalName = declaredClass.getCanonicalName();
                if (canonicalName.equals("java.lang.Object")) {
                    field = new AnyField(name);
                } else {
                    throw new TableSchemaException("Field " + name + " could not be mapped, class: " + declaredClass.getName());
                }
            }
            fields.add(field);
        }
        setAnnotatedFieldMap(createAnnotatedFieldMap(beanClass));
        fieldMap = createFieldMap(fields);
        processAnnotations(fieldMap, annotatedFieldMap);
        this.csvSchema = csvSchema;
    }

    static Map<String, Field<?>> createFieldMap(Collection<Field<?>> fields) {
        Map<String, Field<?>> fieldMap = new LinkedHashMap<>();
        fields.forEach((f) -> fieldMap.put(f.getName(), f));
        return fieldMap;
    }

    static Map<String, AnnotatedField> createAnnotatedFieldMap(Class<?> type) {
        Map<String, AnnotatedField> fields = new LinkedHashMap<>();
        BeanDescription desc = getBeanDescription(type);
        List<BeanPropertyDefinition> properties = desc.findProperties();
        for (BeanPropertyDefinition def : properties) {
            AnnotatedField field = def.getField();
            if (null != field) {
                String declaredName = def.getName();
                fields.put(declaredName, field);
            }
        }
        return fields;
    }

    private static void processAnnotations(Map<String, Field<?>> fieldMap, Map<String, AnnotatedField> annotatedFieldMap) {
        annotatedFieldMap.forEach((k,v) -> {
            AnnotatedField aF = v;
            Field<?> field = fieldMap.get(k);
            FieldFormat fieldFormat = aF.getAnnotation(FieldFormat.class);
            if (null != fieldFormat) {
                String format = fieldFormat.format();
                if (StringUtils.isEmpty(format)) {
                    format = "default";
                }
                field.setFormat(format);
            }
        });
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
