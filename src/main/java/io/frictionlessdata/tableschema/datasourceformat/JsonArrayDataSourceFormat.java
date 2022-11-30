package io.frictionlessdata.tableschema.datasourceformat;

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
 *
 */
public class JsonArrayDataSourceFormat extends AbstractDataSourceFormat {

    public JsonArrayDataSourceFormat(String json){
        super();
        this.dataSource = JsonUtil.getInstance().createArrayNode(DataSourceFormat.trimBOM(json));
    }

    public JsonArrayDataSourceFormat (InputStream inStream) throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader)) {
            String content = br.lines().collect(Collectors.joining("\n"));
            this.dataSource = JsonUtil.getInstance().createArrayNode(DataSourceFormat.trimBOM(content));
        }
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
		Set<String> headers = new LinkedHashSet<>();
		((ArrayNode)dataSource).elements().forEachRemaining((firstObject) -> {
			firstObject.fields().forEachRemaining(f -> {
				headers.add(f.getKey());
			});
		});
		return headers.toArray(new String[]{});
	}
}
