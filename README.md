# tableschema-java
[![Build Status](https://travis-ci.org/frictionlessdata/tableschema-java.svg?branch=master)](https://travis-ci.org/frictionlessdata/tableschema-java)
[![Coverage Status](https://coveralls.io/repos/github/frictionlessdata/tableschema-java/badge.svg?branch=master)](https://coveralls.io/github/frictionlessdata/tableschema-java?branch=master)
[![Gitter](https://img.shields.io/gitter/room/frictionlessdata/chat.svg)](https://gitter.im/frictionlessdata/chat)

A Java library for working with Table Schema.


## Usage

### Parse a CSV

Cast data from a CSV without a schema

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
Table table = new Table(url);

// Iterate through rows          
Iterator<String[]> iter = table.iterator();
while(iter.hasNext()){
    String[] row = iter.next();
    System.out.println(Arrays.toString(row));
}

// [1, foo]
// [2, bar]
// [3, baz]

// Read the entire CSV and output it as a List:
List<String[]> allData = table.read();
```

### Infer a schema

If you don't have a schema for a CSV, and want to generate one, you can infer a schema like so:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
Table table = new Table(url);

JSONObject schema = table.inferSchema();
System.out.println(schema);

// {"fields":[{"name":"id","format":"","description":"","title":"","type":"integer","constraints":{}},{"name":"title","format":"","description":"","title":"","type":"string","constraints":{}}]}

```

### Build a Schema

You can also build a schema from scratch or modify an existing one:

```java
Schema schema = new Schema();

Field nameField = new Field("name", "string");
schema.addField(nameField);

Field coordinatesField = new Field("coordinates", "geopoint");
schema.addField(coordinatesField);

System.out.println(schema.getJson());

// {"fields":[{"name":"name","format":"default","description":"","type":"string","title":"","constraints":{}},{"name":"coordinates","format":"default","description":"","type":"geopoint","title":"","constraints":{}}]}
```

You can also buid a Schema with JSONObject instances instead of Field instances:

```java
Schema schema = new Schema();

JSONObject nameFieldJsonObject = new JSONObject();
nameFieldJsonObject.put("name", "name");
nameFieldJsonObject.put("type", "string");
schema.addField(nameFieldJsonObject);

// An invalid Field definition, will be ignored.
JSONObject invalidFieldJsonObject = new JSONObject();
invalidFieldJsonObject.put("name", "id");
invalidFieldJsonObject.put("type", "integer");
invalidFieldJsonObject.put("format", "invalid");
schema.addField(invalidFieldJsonObject);

JSONObject coordinatesFieldJsonObject = new JSONObject();
coordinatesFieldJsonObject.put("name", "coordinates");
coordinatesFieldJsonObject.put("type", "geopoint");
coordinatesFieldJsonObject.put("format", "array");
schema.addField(coordinatesFieldJsonObject);

System.out.println(schema.getJson());

// {"fields":[{"name":"name","type":"string"},{"name":"coordinates","format":"array","type":"geopoint"}]}
```

Every time a field is added, the schema does a validation test.
If adding a field causes the schema to fail validation, then the field is automatically removed.