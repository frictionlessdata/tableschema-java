package io.frictionlessdata.tableschema.schema;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.SchemaRegistry;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class to validate a JSON document against a JSON schema (https://json-schema.org/).
 * Mostly used to validate the frictionlessdata table-schema.json at
 * https://specs.frictionlessdata.io/schemas/table-schema.json, but also for topo and geojson schemas used
 * in the respective fields.
 *
 * This class uses the networknt JSON schema validator to validate the JSON document.
 */
public class FormalSchemaValidator {

	private static final Map<String, String> LOCAL_SCHEMA_MAPPINGS = buildLocalSchemaMappings();

	private final com.networknt.schema.Schema jsonSchema;

	/**
	 * Instantiate a new FormalSchemaValidator with a JSON Schema.
	 * All occurring validation errors will be returned by `validate()`.
	 *
	 * @param schemaNode the schema to validate against as a JsonNode
	 */
	private FormalSchemaValidator(JsonNode schemaNode) {
		SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
				new TableSchemaVersion().getInstance(),
				builder -> builder.schemas(LOCAL_SCHEMA_MAPPINGS));
		this.jsonSchema = registry.getSchema(schemaNode);
	}

	private static Map<String, String> buildLocalSchemaMappings() {
		Map<String, String> mappings = new LinkedHashMap<>();
		mapLocalSchema(mappings,
				"https://raw.githubusercontent.com/nhuebel/TopoJSON_schema/master/bbox.json",
				"/schemas/topojson-schema/bbox.json");
		mapLocalSchema(mappings,
				"https://raw.githubusercontent.com/nhuebel/TopoJSON_schema/master/geometry.json",
				"/schemas/topojson-schema/geometry.json");
		mapLocalSchema(mappings,
				"https://raw.githubusercontent.com/nhuebel/TopoJSON_schema/master/topology.json",
				"/schemas/topojson-schema/topology.json");
		return mappings;
	}

	private static void mapLocalSchema(Map<String, String> mappings, String iri, String resourcePath) {
		try (InputStream stream = FormalSchemaValidator.class.getResourceAsStream(resourcePath)) {
			if (stream == null) {
				throw new IllegalStateException("Schema resource not found: " + resourcePath);
			}
			mappings.put(iri, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read schema resource: " + resourcePath, e);
		}
	}

	public static FormalSchemaValidator fromJson(String jsonSchema) {
		return new FormalSchemaValidator(JsonUtil.getInstance().readValue(jsonSchema));
	}

	public static FormalSchemaValidator fromJson(InputStream jsonSchema) {
		return new FormalSchemaValidator(JsonUtil.getInstance().readValue(jsonSchema));
	}

	/**
	 * Validate the given JSON document against the schema and return validation messages.
	 * If the document is valid, an empty list is returned.
	 * If the document is invalid, a list of validation messages is returned.
	 * @param json the JSON document to validate
	 * @return validation messages if the document is invalid, an empty list otherwise
	 */
	public List<String> validate(String json) {
		return validate(JsonUtil.getInstance().readValue(json));
	}

	/**
	 * Validate the given JSON document against the schema and return validation messages.
	 * If the document is valid, an empty list is returned.
	 * If the document is invalid, a list of validation messages is returned.
	 * @param json the JSON document to validate
	 * @return validation messages if the document is invalid, an empty list otherwise
	 */
	public List<String> validate(JsonNode json) {
        return jsonSchema.validate(json)
                .stream()
                .map(com.networknt.schema.Error::getMessage)
                .collect(Collectors.toList());

	}

	public String getName() {
		return (null == jsonSchema) ? null : jsonSchema.getSchemaNode().get("title").asText();
	}

}
