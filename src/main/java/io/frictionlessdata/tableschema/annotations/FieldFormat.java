/*
  Copyright [2013-2014] eBay Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package io.frictionlessdata.tableschema.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to annotate a field to represent a column with format information similar to
 * the ´format` property of a Schema entry .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldFormat {

    /**
     * currently valid for
     *
     * - {@link io.frictionlessdata.tableschema.field.DateField}
     * - {@link io.frictionlessdata.tableschema.field.TimeField}
     * - {@link io.frictionlessdata.tableschema.field.DatetimeField}
     *
     * From the specs. https://specs.frictionlessdata.io/table-schema/#types-and-formats
     *  date/time values in this field can be parsed according to
     * &lt;PATTERN&gt;. &lt;PATTERN&gt; MUST follow the syntax of standard Python / C
     * strptime (That is, values in the this field should be parsable
     * by Python / C standard strptime using &lt;PATTERN&gt;).
     * Example for "format": "%d/%m/%y" which would correspond to dates like: 30/11/14
     */
    String format() default "default";


}
