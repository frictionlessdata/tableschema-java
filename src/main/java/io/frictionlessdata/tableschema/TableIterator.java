package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.util.Iterator;

/**
 *
 * 
 */
public class TableIterator<T> implements Iterator<Object[]> {
    
    private Schema schema = null;
    private Iterator<String[]> iter = null;
    private boolean keyed = false;
    private boolean extended = false; 
    private boolean cast = false;
    private boolean relations = false;
    
    public TableIterator(Schema schema, Iterator iter, boolean keyed, boolean extended, boolean cast, boolean relations){
        this.schema = schema;
        this.iter = iter;
        this.keyed = keyed;
        this.extended = extended;
        this.cast = cast;
        this.relations = relations;
        
    }
    
    @Override
    public boolean hasNext() {
        return this.iter.hasNext();
    }

    @Override
    public Object[] next(){
        String[] row = this.iter.next();
        Object[] castedRow = new Object[row.length];
        
        // If there's a schema, attempt to cast the row.
        if(this.schema != null){
            try{
                for(int i=0; i<row.length; i++){
                    Field field = this.schema.getFields().get(i);
                    castedRow[i] = field.castValue(row[i], this.cast);
                }
            }catch(InvalidCastException | ConstraintsException e){
                // The row data types do not match schema definition.
                // Or the row values do not respect the Constraint rules.
                // Do noting and string with String[] typed row.
                castedRow = null;   
            }
            
        }else{
            castedRow = null;  
        }
        
        if(castedRow != null){
            return castedRow;
            
        }else{
            return row;
        }
    }

}
