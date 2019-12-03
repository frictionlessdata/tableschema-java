package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.field.Field;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 */
public class TableIterator<T> implements Iterator<T> {

    private String[] headers = null;
    private Schema schema = null;
    private Iterator<String[]> iter = null;
    private boolean keyed = false;
    private boolean extended = false;
    private boolean cast = true;
    private boolean relations = false;
    private int index = 0;

    public TableIterator(Table table) throws Exception{
        this(table, false, false, true, false);
    }


    public TableIterator(Table table, boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
        this.init(table);
        this.keyed = keyed;
        this.extended = extended;
        this.cast = cast;
        this.relations = relations;
    }

    private void init(Table table) throws Exception{
        this.headers = table.getHeaders();
        this.schema = table.schema();
        this.iter = table.dataSource().iterator();
    }

    @Override
    public boolean hasNext() {
        return this.iter.hasNext();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T next() {
        String[] row = this.iter.next();

        Map<String, Object> keyedRow = new HashMap();
        Object[] extendedRow = new Object[3];
        Object[] castRow = new Object[row.length];

        // If there's a schema, attempt to cast the row.
        if(this.schema != null){
            for(int i=0; i<row.length; i++){
                Field field = this.schema.getFields().get(i);
                Object val = field.castValue(row[i], true, null);

                if(!extended && keyed){
                    keyedRow.put(this.headers[i], val);
                }else{
                    castRow[i] = val;
                }
            }

            if(extended){
                extendedRow = new Object[]{index, this.headers, castRow};
                index++;
                return (T)extendedRow;

            }else if(keyed && !extended){
                return (T)keyedRow;

            }else if(!keyed && !extended){
                return (T)castRow;

            }else{
                return (T)row;
            }
        }else{
            // Enter here if no Schema has been defined.
            if(extended){
                extendedRow = new Object[]{index, this.headers, row};
                index++;
                return (T)extendedRow;

            }else if(keyed && !extended){
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
