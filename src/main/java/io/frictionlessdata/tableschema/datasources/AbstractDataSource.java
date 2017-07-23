package io.frictionlessdata.tableschema.datasources;

import java.util.List;

/**
 *
 * 
 */
public abstract class AbstractDataSource implements DataSource {  
    abstract public String[] readNext() throws Exception;
    abstract public List<String[]> readAll() throws Exception;
    abstract public void save(String outputDataSource) throws Exception;
}
