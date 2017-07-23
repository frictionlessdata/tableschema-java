/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema.datasources;

import java.util.List;

/**
 *
 * @author pechorin
 */
public interface DataSource {
    
    public String[] readNext() throws Exception;
    public List<String[]> readAll() throws Exception;
    public void save(String filename) throws Exception ;  
}
