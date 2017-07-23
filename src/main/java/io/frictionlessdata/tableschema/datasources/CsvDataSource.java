package io.frictionlessdata.tableschema.datasources;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
/**
 *
 * 
 */
public class CsvDataSource extends AbstractDataSource {
    private CSVReader reader = null;
    
    public CsvDataSource(String dataSource) throws FileNotFoundException{
        FileReader fileReader = new FileReader(dataSource);
        this.reader = new CSVReader(fileReader);  
    }


    @Override
    public String[] readNext() throws Exception {
       return this.reader.readNext();
    }
    
    @Override
    public List<String[]> readAll() throws Exception {
       return this.reader.readAll();
    }

    @Override
    public void save(String outputDataSource) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(outputDataSource));
        
        //Write all the rows to file
        writer.writeAll(this.readAll());
        
        //close the writer
        writer.close();
    }

}
