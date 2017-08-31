/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema.datasources;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public interface DataSource {  
    public Iterator<String[]> iterator();
    public String[] getHeaders();
    public List<String[]> data();
    public void write(String outputFilePath) throws IOException;
}
