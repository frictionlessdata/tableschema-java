package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import org.json.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public class Table{
    private DataSource dataSource = null;
    private JSONObject schema = null;
    private TypeInferrer typeInferrer = null;
    
    public Table(String dataSourceFilename) throws Exception{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.typeInferrer = new TypeInferrer();
        // Infer schema?
    }
    
    public Table(URL url) throws Exception{
        this.dataSource = new CsvDataSource(url);
        this.typeInferrer = new TypeInferrer();
        // Infer schema?
    }
    
    public Table(String dataSourceFilename, JSONObject schema) throws Exception{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.typeInferrer = new TypeInferrer();
    }
    
    public Iterator<String[]> iterator(){
        return this.dataSource.iterator();
    }
    
    public String[] headers(){
        if(!this.dataSource.data().isEmpty()){
            return this.dataSource.data().get(0);
        }else{
            return null;
        }
    }
    
    public void save(String filename) throws Exception{
       this.dataSource.save(filename);
    }
    
    public List<String[]> read() throws Exception{
       return this.dataSource.data();
    }
    
    public JSONObject inferSchema() throws TypeInferringException{
        try{
            return this.typeInferrer.infer(this.read(), this.headers());
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    public JSONObject inferSchema(int rowLimit) throws TypeInferringException{
        try{
            this.typeInferrer.infer(this.read(), this.headers(), rowLimit);
            return null;
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
}
