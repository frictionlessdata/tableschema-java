package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import java.io.FileNotFoundException;
import java.util.Iterator;

/**
 *
 * 
 */
public class Table implements Iterator{
    private DataSource dataSource = null;
    private Iterator<String[]> iterator = null;
    private String schema = null;
    
    public Table(String dataSourceFilename) throws Exception{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.iterator = this.dataSource.readAll().iterator();
        
        // Infer schema?
    }
    
    public Table(String dataSourceFilename, String schema) throws FileNotFoundException{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.schema = schema;
    }
    
    public void headers() throws Exception{
        this.dataSource.readAll().get(0);
    }
    
    public void save(String filename) throws Exception{
       this.dataSource.save(filename);
    }
    
    public void read() throws Exception{
       this.dataSource.readAll();
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public String[] next() {
        return this.iterator.next();
    }

}
