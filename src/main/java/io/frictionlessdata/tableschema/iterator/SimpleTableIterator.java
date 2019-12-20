package io.frictionlessdata.tableschema.iterator;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;

import java.util.List;
import java.util.Map;

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
        if (null != schema) {
            List<String> schemaHeaders = schema.getFieldNames();
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
