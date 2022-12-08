package io.frictionlessdata.tableschema.exception;

import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.schema.JsonSchema;

import java.util.*;

public class ValidationException extends TableSchemaException {
	
	List<ValidationMessage> validationMessages = new ArrayList<>();

	public ValidationException(String msg) {
		super(msg);
	}
	
	public ValidationException(Exception ex) {
		super(ex);
	}

	public ValidationException(JsonSchema schema, Collection<ValidationMessage> messages) {
		this(String.format("%s: %s", schema, "validation failed"));
		this.validationMessages.addAll(messages);
	}

	public ValidationException(String schemaName, Collection<ValidationException> exceptions) {
		this(String.format("%s: %s", schemaName, "validation failed"));
		final Set<ValidationMessage> messages = new LinkedHashSet<>();
		exceptions.forEach((m) -> {
			messages.addAll(m.validationMessages);
		});
		this.validationMessages.addAll(messages);
	}
}
