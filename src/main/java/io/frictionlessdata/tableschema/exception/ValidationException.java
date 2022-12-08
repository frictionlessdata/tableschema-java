package io.frictionlessdata.tableschema.exception;

import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.schema.JsonSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends TableSchemaException {

	List<ValidationMessage> validationMessages = new ArrayList<>();

	public ValidationException(String msg) {
		super(msg);
	}
	
	public ValidationException(Exception ex) {
		String message = ex.getClass()+": "+ex.getMessage();
	}
	
	public ValidationException(JsonSchema schema, Collection<ValidationMessage> messages) {
		this(String.format("%s: %s", schema.getName(), "validation failed"));
		this.validationMessages.addAll(messages);
	}

	public ValidationException(String name, Collection<ValidationException> errors) {
		super(String.format("%s: %s", name, "validation failed: "+errors.stream().map(Throwable::getMessage).collect(Collectors.joining("\n"))));
		errors.forEach((ex) -> {
			this.validationMessages.addAll(ex.validationMessages);
		});

	}

	public List<ValidationMessage> getValidationMessages() {
		return validationMessages;
	}
}
