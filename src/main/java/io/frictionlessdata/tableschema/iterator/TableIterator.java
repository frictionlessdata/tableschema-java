package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.field.Field;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 */
public class TableIterator<T> implements Iterator<T> {
    String[] headers = null;
    Schema schema = null;
    Iterator<String[]> wrappedIterator = null;
    boolean keyed;
    boolean extended;
    boolean cast;
    boolean relations;
    Map<String, Object> fieldOptions;
    Map<Integer, Integer> mapping = null;
    int index = 0;

    public TableIterator(Table table) throws Exception{
        this(table, false, false, true, false);
    }

    public TableIterator(
            Table table,
            boolean keyed,
            boolean extended,
            boolean cast,
            boolean relations) throws Exception{

        this.init(table);
        this.keyed = keyed;
        this.extended = extended;
        this.cast = cast;
        this.relations = relations;
    }

    void init(Table table) throws Exception{
        this.fieldOptions = table.getFieldOptions();
        this.mapping = table.getSchemaHeaderMapping();
        this.headers = table.getHeaders();
        this.schema = table.getSchema();
        table.validate();
        this.wrappedIterator = table.getDataSourceFormat().iterator();
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
            rowLength = (row.length > this.schema.getFields().size())
                    ? row.length
                    : this.schema.getFields().size();
        }
        Map<String, Object> keyedRow = new HashMap<>();
        Object[] extendedRow;
        Object[] castRow = new Object[rowLength];
        Object[] plainRow = new Object[rowLength];

        // If there's a schema, attempt to cast the row.
        if(this.schema != null){
            for(int i = 0; i < rowLength; i++){
                Field field = this.schema.getFields().get(i);
                Integer key = mapping.get(i);
                Object val = null;
                // null keys can happen for JSON arrays of JSON objects because
                // null values will lead to missing entries
                if (null != key) {
                    String rawVal = row[mapping.get(i)];
                    val = field.castValue(rawVal, true, fieldOptions);
                }
                if (!extended && keyed) {
                    keyedRow.put(this.headers[i], val);
                } else if (cast){
                    castRow[i] = val;
                } else {
                    plainRow[i] = val;
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
