package io.frictionlessdata.tableschema.schema;

import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.fk.ForeignKey;

import java.util.List;

/**
 * Interface for Schema implementations according to the
 * Table Schema specification from https://specs.frictionlessdata.io//table-schema/index.html
 *
 * Table Schema is a simple language- and implementation-agnostic way to declare a schema for tabular data.
 * Table Schema is well suited for use cases around handling and validating tabular data in text formats such as CSV,
 * but its utility extends well beyond this core usage, towards a range of applications where data benefits
 * from a portable schema format
 */
public interface SchemaInterface {

    /**
     * Get the fields of the schema.
     * @return a list of fields
     */
    List<Field<?>> getFields();

    /**
     * Retrieve a field by its `name` property.
     *
     * @param name the name of the field
     * @return the matching Field or null if no name matches
     */
    Field<?> getField(String name);

    /**
     * Add a field to the schema.
     * @param field the field to add
     */
    void addField(Field<?> field);

    /**
     * Return all the foreign key definitions of the schema.
     *
     * @return a list of {@link ForeignKey} objects
     */
    List<ForeignKey> getForeignKeys();

    /**
     * Check if schema is valid or not.
     *
     * @return true if schema is valid
     */
    boolean isValid();

    /**
     * Check if schema has any fields.
     *
     * @return true if schema has no fields
     */
    boolean isEmpty();

    /**
     * Validate the loaded Schema. Failed validation
     * will lead to a ValidationException thrown.
     *
     * @throws ValidationException If validation fails and validation is strict
     */
    void validate();

    /**
     * Get the header names of the underlying data source(s).
     *
     * @return header names as a String array
     */
    String[] getHeaders();

    /**
     * return the errors that occurred during validation
     * @return a list of ValidationExceptions
     */
    List<ValidationException>  getErrors();
}
