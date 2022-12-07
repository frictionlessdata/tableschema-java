package io.frictionlessdata.tableschema.tabledatasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public class StringArrayTableDataSource extends AbstractTableDataSource {
    private final String[] headers;

    public StringArrayTableDataSource(Collection<String[]> data, String[] headers){
        this.dataSource = data;
        this.headers = headers;
    }

    @Override
    public Iterator<String[]> iterator(){
        return ((Collection<String[]>)dataSource).iterator();
    }

    @Override
    public List<String[]> getDataAsStringArray() {
        return new ArrayList<>((Collection<String[]>)dataSource);
    }

    @Override
    public String[] getHeaders(){
        return headers;
    }

    @Override
    public boolean hasReliableHeaders() {
        return headers != null;
    }
}
