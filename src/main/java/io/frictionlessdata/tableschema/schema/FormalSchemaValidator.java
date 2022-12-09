package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

public class FormalSchemaValidator {
	
	private static final Logger log = LoggerFactory.getLogger(FormalSchemaValidator.class);
	
	private final boolean strictValidation;
	private final JsonSchema jsonSchema;
	
	private FormalSchemaValidator(JsonNode schemaNode, boolean strictValidation) {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V4);
		this.jsonSchema = factory.getSchema(schemaNode);
		this.strictValidation = strictValidation;
	}

	public static FormalSchemaValidator fromJson(String jsonSchema) {
		return fromJson(jsonSchema, true);
	}
	
	public static FormalSchemaValidator fromJson(String jsonSchema, boolean strictValidation) {
		return new FormalSchemaValidator(JsonUtil.getInstance().readValue(jsonSchema), strictValidation);
	}

	public static FormalSchemaValidator fromJson(InputStream jsonSchema, boolean strictValidation) {
		return new FormalSchemaValidator(JsonUtil.getInstance().readValue(jsonSchema), strictValidation);
	}
	
	public Set<ValidationMessage> validate(String json) {
		return validate(JsonUtil.getInstance().readValue(json));
	}
	
	public Set<ValidationMessage> validate(JsonNode json) {
		Set<ValidationMessage> errors = jsonSchema.validate(json);
		if (errors.isEmpty()) {
			return new LinkedHashSet<>();
		} else {
			String msg = String.format("validation failed: %s", errors);
			if (this.strictValidation) {
				log.warn(msg);
				throw new ValidationException(this, errors);
			} else {
				log.warn(msg);
				return errors;
			}
		}
	}

	public String getName() {
		return (null == jsonSchema) ? null : jsonSchema.getSchemaNode().get("title").asText();
	}

}
