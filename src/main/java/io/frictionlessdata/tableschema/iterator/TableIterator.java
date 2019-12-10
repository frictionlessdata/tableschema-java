package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.Schema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
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
    int index = 0;

    TableIterator() {    }

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
        this.headers = table.getHeaders();
        this.schema = table.schema();
        this.wrappedIterator = table.dataSource().iterator();
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

        Map<String, Object> keyedRow = new HashMap();
        Object[] extendedRow;
        Object[] castRow = new Object[row.length];

        // If there's a schema, attempt to cast the row.
        if(this.schema != null){
            for(int i=0; i<row.length; i++){
                Field field = this.schema.getFields().get(i);
                Object val = field.castValue(row[i], true, fieldOptions);

                if(!extended && keyed){
                    keyedRow.put(this.headers[i], val);
                }else{
                    castRow[i] = val;
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
                return (T)row;
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
