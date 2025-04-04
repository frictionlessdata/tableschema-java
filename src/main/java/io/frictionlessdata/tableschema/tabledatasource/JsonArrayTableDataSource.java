package io.frictionlessdata.tableschema.tabledatasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a {@link TableDataSource} based on a Jackson {@link ArrayNode} holding JSON-encoded table.
 *
 * The outer structure is a JSON array, and each row in the tabular data is either:
 * - an {@link ObjectNode} where the key to each property is the row name and the value is the column value.
 * - an {@link ArrayNode} where the values are the column values.
 *
 * Since JSON objects might omit properties with a `null` value, it is hard to recreate the headers without
 * possibly missing some column names, and since in a JSON object, the order of properties need not be preserved,
 * it is not possible to recreate the header column order reliably. Recreating the headers is therefore a
 * very costly operation that has to iterate through the data to extract the column names.
 */
public class JsonArrayTableDataSource extends AbstractTableDataSource<ArrayNode> {

    public JsonArrayTableDataSource (InputStream inStream) throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader)) {
            String content = br.lines().collect(Collectors.joining("\n"));
            dataSource = JsonUtil.getInstance().createArrayNode(TableDataSource.trimBOM(content));
        }
    }
	protected JsonArrayTableDataSource (ArrayNode json){
		dataSource = json;
	}

	public JsonArrayTableDataSource (String json){
		this(JsonUtil.getInstance().createArrayNode(TableDataSource.trimBOM(json)));
	}

    @Override
    public boolean hasReliableHeaders() {
        return false;
    }


	@Override
	public Iterator<String[]> iterator() {
		boolean deleteHeaderRow = (null == this.headers);
		this.headers = getHeaders();
		JsonNode firstRow = dataSource.get(0);
		if ((deleteHeaderRow) && (firstRow instanceof ArrayNode)) {
			dataSource.remove(0);
		}

		return Iterators.transform(dataSource.iterator(), (JsonNode input) -> {
			List<String> values = new ArrayList<>();
			if (input instanceof ObjectNode) {
				for (String header : headers) {
					JsonNode val = input.get(header);
					if (null == val) {
						values.add(null);
					} else if ((val instanceof ObjectNode) || (val instanceof ArrayNode)) {
						values.add(val.toString());
					} else
						values.add(val.asText(""));
				}
			} else if (input instanceof ArrayNode) {
				Iterator<JsonNode> elements = ((ArrayNode) input).elements();
				while (elements.hasNext()) {
					values.add(elements.next().asText(""));
				}
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
	@Override
    public String[] getHeaders() {
		if (null != this.headers) {
			return this.headers;
		}
		Set<String> headers = new LinkedHashSet<>();
		Iterator<JsonNode> iterator = dataSource.elements();
		if (iterator.hasNext()) {
			JsonNode first = iterator.next();
			if (first instanceof ObjectNode) {
				headers.addAll(getColumnNames((ObjectNode) first));
				iterator.forEachRemaining((nextObject) -> {
					headers.addAll(getColumnNames((ObjectNode) nextObject));
				});
			} else if (first instanceof ArrayNode) {
				((ArrayNode) first).elements().forEachRemaining(f -> {
					headers.add(f.asText());
				});
			}
		}

		return headers.toArray(new String[]{});
	}

	private Set<String> getColumnNames(ObjectNode node) {
		Set<String> columnNames = new LinkedHashSet<>();
		node.fieldNames().forEachRemaining(columnNames::add);
		return columnNames;
	}
}
