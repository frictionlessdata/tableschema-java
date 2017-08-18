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
    public Iterator<String[]> iterator();
    public List<String[]> data();
    public void save(String filename) throws Exception ;  
}
