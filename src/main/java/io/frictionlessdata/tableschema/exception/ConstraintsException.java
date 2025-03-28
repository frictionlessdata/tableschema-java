/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema.exception;

/**
 *
 * @author pechorin
 */
public class ConstraintsException extends ValidationException {

    /**
     * Constructs an instance of <code>ConstraintsException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ConstraintsException(String msg) {
        super(msg);
    }
}
