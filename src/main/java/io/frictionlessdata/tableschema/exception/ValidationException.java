package io.frictionlessdata.tableschema.exception;

import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.schema.FormalSchemaValidator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationException extends TableSchemaException {
	List<String> validationMessages = new ArrayList<>();
	List<Exception> wrappedExceptions = new ArrayList<>();

	public ValidationException(String msg) {
		super(msg);
	}
	
	public ValidationException(Exception ex) {
		wrappedExceptions.add(ex);
	}

	public ValidationException(String schemaName, Collection<ValidationMessage> messages) {
		this("Formal validation failed for Schema "+ schemaName);
		addValidationMessages(messages);
	}


	public ValidationException(ValidationException exception) {
		this("Validation failed");
		wrappedExceptions.add(exception);
	}
	public ValidationException(Collection<ValidationException> exceptions) {
		this("Validation failed");
		wrappedExceptions.addAll(exceptions);
	}


	void addValidationMessage(ValidationMessage message) {
		validationMessages.add(message.getMessage());
	}

	void addValidationMessages(Collection<ValidationMessage> messages) {
		messages.forEach(this::addValidationMessage);
	}

	public List<Exception> getWrappedExceptions() {
		return wrappedExceptions;
	}

	public List<String> getValidationMessages() {
		return validationMessages;
	}

	@Override
    public String getMessage(){
		String message = super.getMessage() == null ? "Exception: " : super.getMessage();
		StringBuilder sb = new StringBuilder(message);
		if (!wrappedExceptions.isEmpty()) {
			sb.append(" with ");
			if (wrappedExceptions.size() > 1) {
				sb.append(wrappedExceptions.size()).append(" exceptions: [");
				for (Exception ex : wrappedExceptions) {
					sb.append((ex.getClass().getSimpleName() + ": " + ex.getMessage())).append("\n");
					sb.append("]");
				}
			} else {
					Exception ex = wrappedExceptions.get(0);
					sb.append((ex.getClass().getSimpleName()+": "+ex.getMessage()));
				}
			if (!validationMessages.isEmpty()) {
				sb.append("Additionally, formal validation failed with:\n");
				for (String s : validationMessages) {
					sb.append(s).append("\n");
				}
			}
		} else {
			for (String s : validationMessages) {
				sb.append(s).append("\n");
			}
		}
		return sb.toString();
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
