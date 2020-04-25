package io.frictionlessdata.tableschema.schema;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.util.JsonUtil;

public class JsonSchema {
	
	private final boolean strictValidation;
	private final JsonSchemaFactory factory;
	private com.networknt.schema.JsonSchema jsonSchema;
	
	private JsonSchema(JsonNode schemaNode, boolean strictValidation) {
		this.factory = JsonSchemaFactory.getInstance(VersionFlag.V4);
		this.jsonSchema = factory.getSchema(schemaNode);
		this.strictValidation = strictValidation;
	}

	public static JsonSchema fromJson(String jsonSchema) {
		return fromJson(jsonSchema, false);
	}
	
	public static JsonSchema fromJson(String jsonSchema, boolean strictValidation) {
		return new JsonSchema(JsonUtil.getInstance().readValue(jsonSchema), strictValidation);
	}

	public static JsonSchema fromJson(InputStream jsonSchema, boolean strictValidation) {
		return new JsonSchema(JsonUtil.getInstance().readValue(jsonSchema), strictValidation);
	}
	
	public Set<ValidationMessage> validate(String json) {
		return validate(JsonUtil.getInstance().readValue(json));
	}
	
	public Set<ValidationMessage> validate(JsonNode json) {
		Set<ValidationMessage> errors = jsonSchema.validate(json);
		if (errors.isEmpty()) {
			return Collections.EMPTY_SET;
		} else {
			if (this.strictValidation) {
				throw new ValidationException(this, errors);
			} else {
				return errors;
			}
		}
	}

}
