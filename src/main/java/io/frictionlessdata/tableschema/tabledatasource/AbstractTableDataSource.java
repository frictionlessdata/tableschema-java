package io.frictionlessdata.tableschema.tabledatasource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * 
 */
public abstract class AbstractTableDataSource<T> implements TableDataSource {
    String[] headers;
    T dataSource = null;
    File workDir;

    Charset encoding = StandardCharsets.UTF_8;

    AbstractTableDataSource(){}

    AbstractTableDataSource(T dataSource){
        this.dataSource = dataSource;
    }


    @Override
    public List<String[]> getDataAsStringArray() {
        List<String[]> data = new ArrayList<>();
        iterator().forEachRemaining(data::add);
        return data;
    }

    String getFileContents(String path) throws IOException {
        return TableDataSource.getFileContents(path, workDir, encoding);
    }

    @Override
    public Charset getEncoding() {
        return encoding;
    }
}
