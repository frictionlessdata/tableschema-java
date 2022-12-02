# Creating Schemas

- [Creating from scratch via Java methods](#via-java-methods)
- [Creating from a serialized JSON representation](#from-json)
- [Creating from sample data (inferring)](#inferring-a-schema-from-data)


## Via Java methods

You can build a `Schema` instance from scratch or modify an existing one:

```java
Schema schema = new Schema();

Field nameField = new StringField("name");
schema.addField(nameField);

Field coordinatesField = new GeopointField("coordinates");
schema.addField(coordinatesField);

System.out.println(schema.getJson());

// {"fields":[{"name":"name","format":"default","description":"","type":"string","title":""},{"name":"coordinates","format":"default","description":"","type":"geopoint","title":""}]}
```

## From JSON

You can also build a `Schema` instance with `JSONObject` instances instead of `Field` instances:

```java
Schema schema = new Schema(); // By default strict=false validation

JSONObject nameFieldJsonObject = new JSONObject();
nameFieldJsonObject.put("name", "name");
nameFieldJsonObject.put("type", Field.FIELD_TYPE_STRING);
schema.addField(nameFieldJsonObject);

// Because strict=false, an invalid Field definition will be included.
// The error will be logged/tracked in the error list schema.getErrors().
JSONObject invalidFieldJsonObject = new JSONObject();
invalidFieldJsonObject.put("name", "id");
invalidFieldJsonObject.put("type", Field.FIELD_TYPE_INTEGER);
invalidFieldJsonObject.put("format", "invalid");
schema.addField(invalidFieldJsonObject);

JSONObject coordinatesFieldJsonObject = new JSONObject();
coordinatesFieldJsonObject.put("name", "coordinates");
coordinatesFieldJsonObject.put("type", Field.FIELD_TYPE_GEOPOINT);
coordinatesFieldJsonObject.put("format", Field.FIELD_FORMAT_ARRAY);
schema.addField(coordinatesFieldJsonObject);

System.out.println(schema.getJson());

/* 
{"fields":[
    {"name":"name","format":"default","type":"string"},
    {"name":"id","format":"invalid","type":"integer"},
    {"name":"coordinates","format":"array","type":"geopoint"}
]}
*/
```

When using the `addField` method, the schema undergoes validation after every field addition.
If adding a field causes the schema to fail validation, then the field is automatically removed.

Alternatively, you might want to build your `Schema` by loading the schema definition from a JSON file:

```java
String schemaFilePath = "/path/to/schema/file/shema.json";
Schema schema = new Schema(schemaFilePath, true); // enforce validation with strict=true.
```

## Inferring a Schema from data

If you don't have a schema for a CSV and don't want to manually define one then you can generate it:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                                "/src/test/resources/fixtures/data/simple_data.csv");
Table table = Table.fromSource(url);

Schema schema = table.inferSchema();
System.out.println(schema.getJson());

// {"fields":[{"name":"id","format":"","description":"","title":"","type":"integer","constraints":{}},{"name":"title","format":"","description":"","title":"","type":"string","constraints":{}}]}

```

The type inferral algorithm tries to cast to available types and each successful type casting increments a popularity score for the successful type cast in question. At the end, the best score so far is returned.
The inferral algorithm traverses all of the table's rows and attempts to cast every single value of the table. When dealing with large tables, you might want to limit the number of rows that the inferral algorithm processes:

```java
// Only process the first 25 rows for type inferral.
Schema schema = table.inferSchema(25);
```

If `List<Object[]> data` and `String[] headers` are available, the schema can also be inferred from the a Schema object:
```java
JSONObject inferredSchema = schema.infer(data, headers);
```

Row limit can also be set:
```java
JSONObject inferredSchema = schema.infer(data, headers, 25);
```

Using an instance of Table or Scheme to infer a schema invokes the same method from the TypeInferred Singleton:
```java
TypeInferrer.getInstance().infer(data, headers, 25);
```

