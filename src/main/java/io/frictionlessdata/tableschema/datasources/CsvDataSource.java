package io.frictionlessdata.tableschema.datasources;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * 
 */
public class CsvDataSource extends AbstractDataSource {
    private CSVReader reader = null;
    private List<String[]> data = null;
    private String[] headers = null;
    
    public CsvDataSource(String dataSource) throws Exception{
        FileReader fileReader = new FileReader(dataSource);
        this.reader = new CSVReader(fileReader);
        this.data = this.reader.readAll();
        this.headers = this.data.remove(0); // remove header
    }
    
    public CsvDataSource(URL url) throws Exception{
        InputStreamReader inputStreamReader = new InputStreamReader(url.openStream(), "UTF-8");
        this.reader = new CSVReader(inputStreamReader);
        this.data = this.reader.readAll();
        this.headers = this.data.remove(0); // remove header
    }
    
    @Override
    public Iterator<String[]> iterator(){
        return this.data.iterator();
    }
    
    @Override
    public List<String[]> data(){
       return this.data;
    }

    @Override
    public void write(String outputFilePath) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath));
        
        // Write header
        writer.writeNext(this.headers);
        
        //Write all the rows to file
        writer.writeAll(this.data());
        
        //close the writer
        writer.close();
    }
    
    @Override
    public String[] getHeaders(){
        return this.headers;
    }

}
