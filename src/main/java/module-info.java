module frictionlessdata.tableschema {
    requires com.fasterxml.jackson.databind;
    requires  org.apache.commons.lang3;
    requires json.schema.validator;
    requires org.locationtech.jts;
    requires com.google.common;
    requires commons.validator;
    requires commons.csv;
    requires com.fasterxml.jackson.dataformat.csv;
    requires org.geotools.referencing;
    requires org.slf4j;

    exports io.frictionlessdata.tableschema;
    exports io.frictionlessdata.tableschema.annotations;
    exports io.frictionlessdata.tableschema.exception;
    exports io.frictionlessdata.tableschema.field;
    exports io.frictionlessdata.tableschema.fk;
    exports io.frictionlessdata.tableschema.iterator;
    exports io.frictionlessdata.tableschema.schema;
    exports io.frictionlessdata.tableschema.tabledatasource;

    opens io.frictionlessdata.tableschema;
    opens io.frictionlessdata.tableschema.annotations;
    opens io.frictionlessdata.tableschema.exception;
    opens io.frictionlessdata.tableschema.field;
    opens io.frictionlessdata.tableschema.fk;
    opens io.frictionlessdata.tableschema.iterator;
    opens io.frictionlessdata.tableschema.schema;
    opens io.frictionlessdata.tableschema.tabledatasource;
    exports io.frictionlessdata.tableschema.util;

}