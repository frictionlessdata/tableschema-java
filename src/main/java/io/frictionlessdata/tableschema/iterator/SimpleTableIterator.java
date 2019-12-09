package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.field.Field;

import java.util.HashMap;
import java.util.Map;

public class SimpleTableIterator extends TableIterator<String[]> {

    public SimpleTableIterator(Table table) throws Exception {
        super(table);
    }

    public SimpleTableIterator(
            Table table,
            boolean keyed,
            boolean extended,
            boolean relations) throws Exception{
        super(table, keyed, extended, false,  relations);
    }

}
