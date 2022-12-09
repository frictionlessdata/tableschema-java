package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.Schema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 *
 */
public class TableIterator<T> implements Iterator<T> {
    String[] headers = null;
    Schema schema = null;
    Iterator<String[]> wrappedIterator = null;
    boolean keyed = false;
    boolean extended = false;
    boolean cast = true;
    boolean relations = false;
    Map<Integer, Integer> mapping = null;
    int index = 0;


    TableIterator() {}

    public TableIterator(Table table) {
        this(table, false, false, true, false);
    }

    public TableIterator(
            Table table,
            boolean keyed,
            boolean extended,
            boolean cast,
            boolean relations){

        this.init(table);
        this.keyed = keyed;
        this.extended = extended;
        this.cast = cast;
        this.relations = relations;
    }

    void init(Table table) {
        this.mapping = table.getSchemaHeaderMapping();
        this.headers = table.getHeaders();
        this.schema = table.getSchema();
        table.validate();
        this.wrappedIterator = table.getTableDataSource().iterator();
    }


    @Override
    public boolean hasNext() {
        return this.wrappedIterator.hasNext();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T next() {
        String[] row = this.wrappedIterator.next();
        int rowLength = row.length;
        if (null != this.schema) {
            rowLength = Math.max(row.length, this.schema.getFields().size());
        }
        Map<String, Object> keyedRow = new LinkedHashMap<>();
        Object[] extendedRow;
        Object[] castRow = new Object[rowLength];
        Object[] plainRow = new Object[rowLength];

        // If there's a schema, attempt to cast the row.
        if(this.schema != null){
            for(int i = 0; i < rowLength; i++){
                Field field = this.schema.getFields().get(i);
                Object val = null;
                // if the CSVFormat does not specify a header row, mapping will be null and we use the
                // row order from the Schema
                Integer mappedKey = i;
                if (null != mapping) {
                    if (null != mapping.get(i))
                        mappedKey = mapping.get(i);
                    else
                        mappedKey = null;

                }
                // null keys can happen for JSON arrays of JSON objects because
                // null values will lead to missing entries
                if (null != mappedKey) {
                    // if the last column(s) contain nulls, prevent an ArrayIndexOutOfBoundsException
                    if (mappedKey < row.length) {
                        String rawVal = row[mappedKey];
                        val = field.castValue(rawVal);
                    }
                }
                if (!extended && keyed) {
                    keyedRow.put(this.headers[i], val);
                } else if (cast || extended){
                    castRow[i] = val;
                } else {
                    plainRow[i] = field.formatValueAsString(val);
                }
            }

            if (extended){
                extendedRow = new Object[]{index, this.headers, castRow};
                index++;
                return (T)extendedRow;
            } else if(keyed){
                return (T)keyedRow;
            } else if(cast){
                return (T)castRow;
            } else{
                return (T)plainRow;
            }
        }else{
            // Enter here if no Schema has been defined.
            if(extended){
                extendedRow = new Object[]{index, this.headers, row};
                index++;
                return (T)extendedRow;

            }else if(keyed){
                keyedRow = new HashMap();
                for(int i=0; i<row.length; i++){
                    keyedRow.put(this.headers[i], row[i]);
                }
                return (T)keyedRow;

            }else{
                return (T)row;
            }
        }
    }
}
