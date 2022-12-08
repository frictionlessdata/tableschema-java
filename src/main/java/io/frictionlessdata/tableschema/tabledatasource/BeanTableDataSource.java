package io.frictionlessdata.tableschema.tabledatasource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a {@link TableDataSource} based on a Java Bean class.
 */
public class BeanTableDataSource<C> extends AbstractTableDataSource<C> {

	private List<C> beans;

    public BeanTableDataSource(Collection<C> data) {
		beans = new ArrayList<>(data);
    }

	public BeanTableDataSource(C[] data){
		beans = Arrays.asList(data);
	}

	public BeanTableDataSource(ArrayNode json){
		beans = JsonUtil.getInstance().deserialize(json, new TypeReference<List<C>>() {});
	}


    @Override
    public boolean hasReliableHeaders() {
        return false;
    }


	@Override
	public Iterator<String[]> iterator() {
		String[] headers = getHeaders();

		return Iterators.transform(((ArrayNode)dataSource).iterator(), (JsonNode input) -> {
			List<String> values = new ArrayList<>();
			for (String header : headers) {
				JsonNode val = input.get(header);
				if (null == val) {
					values.add(null);
				} else if ((val instanceof ObjectNode) || (val instanceof ArrayNode)) {
					values.add(val.toString());
				} else
					values.add(val.asText(""));
			}

			return values.toArray(new String[0]);
		});
	}

	/**
	 * This is a very costly operation that iterates through the whole data to find the headers. See
	 * https://github.com/frictionlessdata/specs/issues/656#issuecomment-574386328 for the background: missing
	 * (== null) entries in the first entry sets of a JSON Table could lead to us missing header names.
	 * @return the union of keys for each entry in the JSON array
	 */
	public String[] getHeaders() {

	}
}
