package io.frictionlessdata.tableschema.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.frictionlessdata.tableschema.exception.JsonParsingException;
import io.frictionlessdata.tableschema.exception.JsonSerializingException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.io.InputStream;
import java.util.Objects;

public final class JsonUtil {
	private static JsonUtil instance;
	private ObjectMapper mapper;
	
	private JsonUtil() {
		this.mapper = JsonMapper.builder()
			.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
			.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
			.changeDefaultPropertyInclusion(i -> i
				.withValueInclusion(JsonInclude.Include.NON_NULL)
				.withContentInclusion(JsonInclude.Include.NON_NULL))
			.findAndAddModules()
			.build();
	}
	
	public static JsonUtil getInstance() {
		if (Objects.isNull(instance)) {
			instance = new JsonUtil();
		}
		return instance;
	}

	public ObjectMapper getMapper(){return mapper;}


	public ObjectNode createNode() {
		return mapper.createObjectNode();
	}
	
	public StringNode createStringNode(String value) {
		return new StringNode(value);
	}
	
	public JsonNode createNode(String content) {
		try {
			return mapper.readTree(content);
		} catch (JacksonException e) {
			throw new JsonParsingException(e);
		}
	}
	
	public JsonNode createNode(Object content) {
		try {
			String json = mapper.writeValueAsString(content);
			try {
				return mapper.readTree(json);
			} catch (JacksonException e) {
				throw new JsonParsingException(e);
			} 
		} catch (JacksonException e) {
			throw new JsonSerializingException(e);
		}
	}
	
	public ArrayNode createArrayNode() {
		return mapper.createArrayNode();
	}
	
	public ArrayNode createArrayNode(String content) {
		try {
			return (ArrayNode)mapper.readTree(content);
		} catch (JacksonException e) {
			throw new JsonParsingException(e);
		}
	}
	
	public ArrayNode createArrayNode(Object content) {
		return (ArrayNode) createNode(content);
	}

	public String serialize(Object value) {
		return serialize (value, true);
	}
	public String serialize(Object value, boolean multiline) {
		try {
			return _getWriter(multiline).writeValueAsString(value);
		} catch (JacksonException e) {
			throw new JsonSerializingException(e);
		}
	}

	public <T> T deserialize(String value, Class<T> clazz) {
		try {
			return mapper.readValue(sanitize(value), clazz);
		} catch (JacksonException e) {
			throw new JsonParsingException(e);
		}
	}
	
	public <T> T deserialize(String value, TypeReference<T> typeRef) {
		try {
			return mapper.readValue(sanitize(value), typeRef);
		} catch (JacksonException e) {
			throw new JsonParsingException(e);
		}
	}

	public <T> T deserialize(JsonNode value, TypeReference<T> typeRef) {
		return mapper.convertValue(value, typeRef);
	}

	public <T> T deserialize(JsonNode value, Class<T> clazz) {
		return mapper.convertValue(value, clazz);
	}

	public JsonNode readValue(String value) {
		try {
			return mapper.readTree(sanitize(value));
		} catch (JacksonException e) {
			throw new JsonParsingException(e);
		}
	}
	
	public JsonNode readValue(InputStream value) {
		try {
			return mapper.readTree(value);
		} catch (Exception e) {
			throw new JsonParsingException(e);
		}
	}

	public <T> T convertValue(Object value, TypeReference<T> ref) {
		return mapper.convertValue(value, ref);
	}
	
	public <T> T convertValue(Object value, Class<T> clazz) {
		return mapper.convertValue(value, clazz);
	}
	
	// if it uses the extended double quote character sometimes found in CSV files
	private String sanitize(String string) {
		if(string.startsWith("[“") || string.startsWith("{“")) {
    		// replace both left and right versions
    		return string.replace("“", "\"").replace("”", "\"");
    	} else return string;
	}

	private ObjectWriter _getWriter(boolean multiline) {
		return (multiline) ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
	}
	
}
