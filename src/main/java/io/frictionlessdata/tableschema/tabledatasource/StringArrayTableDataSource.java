package io.frictionlessdata.tableschema.tabledatasource;

import java.util.*;

/**
 *
 * 
 */
public class StringArrayTableDataSource extends AbstractTableDataSource<Collection<String[]>> {
    private final String[] headers;

    public StringArrayTableDataSource(Collection<String[]> data, String[] headers){
        this.dataSource = data;
        this.headers = headers;
    }

    public StringArrayTableDataSource(String[][] data, String[] headers){
        this.dataSource = Arrays.asList(data);
        this.headers = headers;
    }

    @Override
    public Iterator<String[]> iterator(){
        return dataSource.iterator();
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
