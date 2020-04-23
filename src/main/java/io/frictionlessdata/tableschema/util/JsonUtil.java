package io.frictionlessdata.tableschema.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TableSchemaException;

public final class JsonUtil {

	private static JsonUtil instance;
	private ObjectMapper mapper;
	
	private JsonUtil() {
		this.mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}
	
	public static JsonUtil getInstance() {
		if (Objects.isNull(instance)) {
			instance = new JsonUtil();
		}
		return instance;
	}
	
	public JsonNode createNode() {
		return mapper.createObjectNode();
	}
	
	public JsonNode createNode(String content) {
		try {
			return mapper.readTree(content);
		} catch (JsonProcessingException e) {
			throw new InvalidCastException(e);
		}
	}
	
	public JsonNode createNode(Object content) {
		try {
			return mapper.readTree(mapper.writeValueAsString(content));
		} catch (JsonProcessingException e) {
			throw new InvalidCastException(e);
		}
	}
	
	public ArrayNode createArrayNode(String content) {
		try {
			return (ArrayNode)mapper.readTree(content);
		} catch (JsonProcessingException e) {
			throw new TableSchemaException(e);
		}
	}
	
	public ArrayNode createArrayNode(Object content) {
		try {
			return (ArrayNode)mapper.readTree(mapper.writeValueAsString(content));
		} catch (JsonProcessingException e) {
			throw new TableSchemaException(e);
		}
	}
	
	public String serialize(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new TableSchemaException(e);
		}
	}
	
	public <T> T deserialize(String value, Class<T> clazz) {
		try {
			return mapper.readValue(sanitize(value), clazz);
		} catch (JsonProcessingException e) {
			throw new InvalidCastException(e);
		}
	}
	
	public <T> T deserialize(String value, TypeReference<T> typeRef) {
		try {
			return mapper.readValue(sanitize(value), typeRef);
		} catch (JsonProcessingException e) {
			throw new InvalidCastException(e);
		}
	}
	
	public JsonNode readValue(String value) {
		try {
			return mapper.readTree(sanitize(value));
		} catch (JsonProcessingException e) {
			throw new TableSchemaException(e);
		}
	}
	
	public JsonNode readValue(InputStream value) {
		try {
			return mapper.readTree(value);
		} catch (IOException e) {
			throw new TableSchemaException(e);
		}
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
	
}
