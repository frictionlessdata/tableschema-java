package io.frictionlessdata.tableschema.datasources;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public abstract class AbstractDataSource implements DataSource {

    @Override
    abstract public Iterator<String[]> iterator();
    
    @Override
    abstract public String[] getHeaders();
    
    @Override
    abstract public List<String[]> data();
    
    @Override
    abstract public void write(String outputFilePath) throws IOException;
}
