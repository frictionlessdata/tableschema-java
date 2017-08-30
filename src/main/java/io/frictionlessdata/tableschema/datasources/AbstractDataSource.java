package io.frictionlessdata.tableschema.datasources;

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
    abstract public void save(String outputDataSource) throws Exception;
}
