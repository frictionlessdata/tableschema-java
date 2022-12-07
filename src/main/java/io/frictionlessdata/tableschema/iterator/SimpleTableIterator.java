package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.Table;

public class SimpleTableIterator extends TableIterator<String[]> {

    public SimpleTableIterator(Table table)  {
        super(table);
    }

    public SimpleTableIterator(Table table, boolean relations) {
        this(table);
        this.relations = relations;
    }

    @Override
    public String[] next() {
        String[] row = this.wrappedIterator.next();
        if (null != schema) {
            String[] newRow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                newRow[mapping.get(i)] = row[i];
            }
            return newRow;
        } else {
            return row;
        }
    }

}
