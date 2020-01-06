package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.field.*;
import org.geotools.geometry.DirectPosition2D;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BeanSchema extends Schema{

    private CsvSchema csvSchema;

    private BeanSchema(List<Field> fields, boolean strict) {
        super(fields, strict);
    }


    public static Schema infer(Class beanClass) throws NoSuchFieldException, JsonMappingException {
        List<Field> fields = new ArrayList<>();
        CsvMapper mapper = new CsvMapper();
        mapper.setVisibility(mapper.getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        CsvSchema csvSchema = mapper.typedSchemaFor(beanClass);
        Iterator<CsvSchema.Column> iterator = csvSchema.iterator();
        Map<String, String> fieldNames = ReflectionUtils.getFieldNameMapping(mapper, beanClass);
        while (iterator.hasNext()) {
            CsvSchema.Column next = iterator.next();
            String name = next.getName();
            CsvSchema.ColumnType type = next.getType();
            Field field = null;
            java.lang.reflect.Field declaredField = beanClass.getDeclaredField(fieldNames.get(name));
            java.lang.Class declaredClass = declaredField.getType();
            switch (type) {
                case ARRAY:
                    field = new ArrayField(name);
                    break;
                case STRING:
                    field = new StringField(name);
                    break;
                case BOOLEAN:
                    field = new BooleanField(name);
                    break;
                case NUMBER: {
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
                        field = new NumberField(name);
                    }
                }
                break;
                case NUMBER_OR_STRING: {
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
                    else if (declaredClass.equals(JSONObject.class))
                        field = new ObjectField(name);
                    else if (declaredClass.equals(Map.class))
                        field = new ObjectField(name);
                }
                break;
                default:
                    field = new AnyField(name);
            }
            fields.add(field);
        }
        BeanSchema bs = new BeanSchema(fields, true);
        bs.csvSchema = csvSchema;
        return bs;
    }

    public CsvSchema getCsvSchema() {
        return csvSchema;
    }

    public void setCsvSchema(CsvSchema csvSchema) {
        this.csvSchema = csvSchema;
    }
}
