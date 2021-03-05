package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import org.apache.commons.csv.CSVParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public class StringArrayDataSourceFormat extends AbstractDataSourceFormat {
    private  String[] headers;

    public StringArrayDataSourceFormat(Collection<String[]> data, String[] headers){
        this.dataSource = data;
        this.headers = headers;
    }

    CSVParser getCSVParser() {
        throw new TableSchemaException("Not implemented for StringArrayDataSourceFormat");
    };

    @Override
    public Iterator<String[]> iterator() throws Exception{
        return ((Collection<String[]>)dataSource).iterator();
    }

    @Override
    public List<String[]> data() throws Exception{
        return new ArrayList<>((Collection<String[]>)dataSource);
    }

    @Override
    public String[] getHeaders() throws Exception{
        return headers;
    }

    @Override
    public boolean hasReliableHeaders() {
        return headers != null;
    }
}
