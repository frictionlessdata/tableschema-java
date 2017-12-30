/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema.datasources;

import java.util.Iterator;
import java.util.List;

/**
 *
 */
public interface DataSource {  
    public Iterator<String[]> iterator() throws Exception;
    public String[] getHeaders() throws Exception;
    public List<String[]> data() throws Exception;
    public void write(String outputFilePath) throws Exception;
}
