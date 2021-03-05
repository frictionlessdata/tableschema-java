package io.frictionlessdata.tableschema.exception;

import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.schema.JsonSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationException extends TableSchemaException {
	
	List<ValidationMessage> validationMessages = new ArrayList<>();

	public ValidationException(String msg) {
		super(msg);
	}
	
	public ValidationException(Exception ex) {
		super(ex);
	}
	
	public ValidationException(JsonSchema schema, Collection<ValidationMessage> message) {
		this(String.format("%s: %s", schema, "validation failed"));
		this.validationMessages.addAll(validationMessages);
	}
}
