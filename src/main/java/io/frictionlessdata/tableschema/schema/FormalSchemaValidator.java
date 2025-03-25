package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonNodeReader;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A class to validate a JSON document against the a JSON schema.
 * Mostly used to validate the frictionless data table-schema.json at
 * https://specs.frictionlessdata.io/schemas/table-schema.json, but also for topo and geojson schemas used
 * in the respective fields.
 *
 * This class uses the networknt JSON schema validator to validate the JSON document.
 */
public class FormalSchemaValidator {

	private final JsonSchema jsonSchema;

	/**
	 * Instantiate a new FormalSchemaValidator with the given schemaNode and strictValidation flag.
	 * All occuring validation errors will be returned by `validate()`.
	 * @param schemaNode the schema to validate against as a JsonNode
	 */
	private FormalSchemaValidator(JsonNode schemaNode) {
		Consumer<JsonSchemaFactory.Builder> customizer = builder -> builder.metaSchema(new TableSchemaVersion().getInstance());
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V4, customizer);
		this.jsonSchema = factory.getSchema(schemaNode);
	}

	public static FormalSchemaValidator fromJson(String jsonSchema) {
		return new FormalSchemaValidator(JsonUtil.getInstance().readValue(jsonSchema));
	}

	public static FormalSchemaValidator fromJson(InputStream jsonSchema) {
		return new FormalSchemaValidator(JsonUtil.getInstance().readValue(jsonSchema));
	}
	
	public Set<ValidationMessage> validate(String json) {
		return validate(JsonUtil.getInstance().readValue(json));
	}

	/**
	 * Validate the given JSON document against the schema and return a set of {@link ValidationMessage} objects.
	 * If the document is valid, an empty set is returned.
	 * If the document is invalid, a set of ValidationMessages is returned.
	 * @param json the JSON document to validate
	 * @return a set of ValidationMessages if the document is invalid, an empty set otherwise
	 */
	public Set<ValidationMessage> validate(JsonNode json) {
        return jsonSchema.validate(json);

	}

	public String getName() {
		return (null == jsonSchema) ? null : jsonSchema.getSchemaNode().get("title").asText();
	}

}
