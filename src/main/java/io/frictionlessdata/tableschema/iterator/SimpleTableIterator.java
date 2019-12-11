package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.Table;

public class SimpleTableIterator extends TableIterator<String[]> {

    public SimpleTableIterator(Table table) throws Exception {
        super(table);
    }

    public SimpleTableIterator(Table table, boolean relations) throws Exception{
        this(table);
        this.relations = relations;
    }

    @Override
    public String[] next() {
        String[] row = this.wrappedIterator.next();
        return row;
    }

}
