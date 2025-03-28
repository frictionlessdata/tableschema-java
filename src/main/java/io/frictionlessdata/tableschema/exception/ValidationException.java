package io.frictionlessdata.tableschema.exception;

import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.schema.FormalSchemaValidator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationException extends TableSchemaException {
	List<ValidationMessage> validationMessages = new ArrayList<>();
	List<Exception> wrappedExceptions = new ArrayList<>();

	public ValidationException(String msg) {
		super(msg);
	}
	
	public ValidationException(Exception ex) {
		wrappedExceptions.add(ex);
	}

	public ValidationException(FormalSchemaValidator schema, Collection<ValidationMessage> messages) {
		this(String.format("%s: %s", schema.getName(), "validation failed"));
		this.validationMessages.addAll(messages);
	}

	public ValidationException(String message, String schemaName, Collection<ValidationMessage> messages) {
		this(String.format("%s: %s",  "validation failed: "+message, schemaName));
		this.validationMessages.addAll(messages);
	}

	public ValidationException(String schemaName, Collection<ValidationException> exceptions) {
		this(String.format("%s: %s", schemaName, "validation failed: "));
		wrappedExceptions.addAll(exceptions);
		final Set<ValidationMessage> messages = new LinkedHashSet<>();
		exceptions.forEach((m) -> {
			if (m instanceof ValidationException) {
				messages.addAll(((ValidationException)m).validationMessages);
			}
		});
		this.validationMessages.addAll(messages);
	}

	public List<Exception> getWrappedExceptions() {
		return wrappedExceptions;
	}

	public List<ValidationMessage> getValidationMessages() {
		return validationMessages;
	}

	public List<Object> getMessages() {
		List<Object> retVal = new ArrayList<>();
		retVal.addAll(validationMessages);
		for (Exception ex : wrappedExceptions) {
			retVal.add(ex.getMessage());
		}
		return retVal;
	}
}
