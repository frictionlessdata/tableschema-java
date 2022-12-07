package io.frictionlessdata.tableschema.tabledatasource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public abstract class AbstractTableDataSource implements TableDataSource {
    String[] headers;
    Object dataSource = null;
    private File workDir;

    AbstractTableDataSource(){}

    AbstractTableDataSource(URL dataSource){
        this.dataSource = dataSource;
    }

    AbstractTableDataSource(File dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    AbstractTableDataSource(String dataSource){
        this.dataSource = dataSource;
    }


    @Override
    public List<String[]> getDataAsStringArray() {
        List<String[]> data = new ArrayList<>();
        iterator().forEachRemaining(data::add);
        return data;
    }


    String getFileContents(String path) throws IOException {
        return TableDataSource.getFileContents(path, workDir);
    }

}
