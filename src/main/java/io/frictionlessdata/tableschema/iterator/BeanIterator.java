package io.frictionlessdata.tableschema.iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.field.Field;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeanIterator<T> extends TableIterator<T> {
    private Class<T> type = null;
    private CsvMapper mapper = new CsvMapper();
    private CsvSchema csvSchema;

    public BeanIterator(Table table,  Class<T> beanType) throws Exception {
        super(table);
        this.type = beanType;
        csvSchema = mapper.typedSchemaFor(beanType);
    }

    public BeanIterator(Table table, boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception {
        super(table, keyed, extended, cast, relations);
    }

    @Override
    public T next() {
        T retVal;
        String[] row = super.wrappedIterator.next();

        Map<String, Object> keyedRow = new HashMap();
        Object[] extendedRow;
        Object[] castRow = new Object[row.length];

        // If there's a schema, attempt to cast the row.
        if (this.schema != null) {
            try {
                retVal = type.newInstance();
                String csv = mapper.writer().writeValueAsString(row);
                /*MappingIterator it = mapper.readerFor(type).with(super.schema)
                        .readValue(csv);*/

                for (int i = 0; i < row.length; i++) {
                    Field field = this.schema.getFields().get(i);
                    Object val = field.castValue(row[i], true, fieldOptions);
                    java.lang.reflect.Field declaredField = type.getDeclaredField(field.getName());
                }


                return retVal;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new TableSchemaException(e);
            } catch (IOException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            throw new TableSchemaException("Cannot use a BeanIterator without Schema");
        }
        return null;
    }
}
