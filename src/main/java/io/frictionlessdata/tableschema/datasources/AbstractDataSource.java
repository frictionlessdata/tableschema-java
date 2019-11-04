package io.frictionlessdata.tableschema.datasources;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public abstract class AbstractDataSource implements DataSource {

    @Override
    abstract public Iterator<String[]> iterator() throws Exception;
    
    @Override
    abstract public String[] getHeaders() throws Exception;
    
    @Override
    abstract public List<String[]> data() throws Exception;
    
    @Override
    abstract public void write(File outputFile) throws Exception;

    public void writeCsv(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile));
             CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.RFC4180)) {

            String[] headers = getHeaders();
            if (headers != null) {
                csvPrinter.printRecord((Object[]) headers);
            }

            for (String[] record : data()) {
                csvPrinter.printRecord(record);
            }

            csvPrinter.flush();

        } catch (Exception e) {
            throw e;
        }
    }
}
