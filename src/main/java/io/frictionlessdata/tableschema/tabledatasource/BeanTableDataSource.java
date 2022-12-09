package io.frictionlessdata.tableschema.tabledatasource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.annotations.FieldFormat;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a {@link TableDataSource} based on a Java Bean class.
 */
public class BeanTableDataSource<C> extends AbstractTableDataSource<C> {
	private final Class<C> type;
	private final List<C> beans;

    public BeanTableDataSource(Collection<C> data, Class<C> type) {
		beans = new ArrayList<>(data);
		this.type = type;
    }

	public BeanTableDataSource(C[] data, Class<C> type){
		beans = Arrays.asList(data);
		this.type = type;
	}

	public BeanTableDataSource(ArrayNode json, Class<C> type){
		beans = JsonUtil.getInstance().deserialize(json, new TypeReference<List<C>>() {});
		this.type = type;
	}

	public Class<C> getBeanClass() {
		return type;
	}

    @Override
    public boolean hasReliableHeaders() {
        return true;
    }


	@Override
	public Iterator<String[]> iterator() {
		String[] headers = getHeaders();
		BeanSchema schema = BeanSchema.infer(type);

		return Iterators.transform((beans).iterator(), (C input) -> {
			List<String> values = new ArrayList<>();
			for (String header : headers) {
				Field<Object> schemaField = (Field<Object>)schema.getField(header);
				if (null == schemaField) {
					continue;
				}
				AnnotatedField aF = schema.getAnnotatedField(header);
				aF.fixAccess(true);
				Object fieldValue = aF.getValue(input);

				FieldFormat annotation = aF.getAnnotation(FieldFormat.class);
				String fieldFormat = schemaField.getFormat();
				if (null != annotation) {
					fieldFormat = annotation.format();
				} else if (null != fieldValue){
					// we may have a schemaField that can have different formats
					// but the Schema doesn't know about the true format
					if (fieldFormat.equals(Field.FIELD_FORMAT_DEFAULT)) {
						// have to parse format here when we have actual sample data
						// instead of at BeanSchema inferral time
						fieldFormat = schemaField.parseFormat(fieldValue.toString(), null);
					}
				}
				schemaField.setFormat(fieldFormat);
				String val = schemaField.formatValueAsString(fieldValue);
				values.add(val);
			}

			return values.toArray(new String[0]);
		});
	}

	/**
	 * Let a BeanSchema define the headers.s
	 */
	public String[] getHeaders() {
		BeanSchema bs = BeanSchema.infer(type);
		return bs.getHeaders();
	}
}
