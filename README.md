# tableschema-java

[![Build Status](https://travis-ci.org/frictionlessdata/tableschema-java.svg?branch=master)](https://travis-ci.org/frictionlessdata/tableschema-java)
[![Coverage Status](https://coveralls.io/repos/github/frictionlessdata/tableschema-java/badge.svg?branch=master)](https://coveralls.io/github/frictionlessdata/tableschema-java?branch=master)
[![License](https://img.shields.io/github/license/frictionlessdata/tableschema-java.svg)](https://github.com/frictionlessdata/tableschema-java/blob/master/LICENSE)
[![Release](https://img.shields.io/jitpack/v/github/frictionlessdata/tableschema-java)](https://jitpack.io/#frictionlessdata/tableschema-java)
[![Codebase](https://img.shields.io/badge/codebase-github-brightgreen)](https://github.com/frictionlessdata/tableschema-java)
[![Support](https://img.shields.io/badge/support-discord-brightgreen)](https://discordapp.com/invite/Sewv6av)

A Java library for working with Table Schema. Snapshots on [Jitpack](https://jitpack.io/#frictionlessdata/tableschema-java).
tableschema-java is a library aimed at parsing CSV and JSON-Array documents into a tabular format according 
to [Table Schema](https://frictionlessdata.io/specs/table-schema/), a format definition based on 
[JSON Schema](https://json-schema.org/understanding-json-schema/).

It allows you to read and write tabular data with assurances to format integrity (it also allows reading and writing
CSV free-form, ie. without a Schema).

## Usage
- [Reading data](docs/table-reading.md) explains various ways of reading data
- [Creating a Schema](docs/creating-schemas.md) shows ways of creating a Table Schema
- [As part of Datapackages](https://github.com/frictionlessdata/datapackage-java) to distribute data sets as self-contained units

### Write a Table Into a File

You can write a `Table` into a CSV file:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
Table table = Table.fromSource(url);
table.write("/path/to/write/table.csv");
```


### Write a Schema Into a File:

You can write a `Schema` into a JSON file:

```java
Schema schema = new Schema();

Field nameField = new StringField("name");
schema.addField(nameField);

Field coordinatesField = new GeopointField("coordinates");
schema.addField(coordinatesField);

schema.writeJson(new File("schema.json"));
   
```



### Validate a Schema
To make sure a schema complies with [Table Schema specifications](https://specs.frictionlessdata.io/table-schema/), we can validate each custom schema against the official [Table Schema schema](https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/main/resources/schemas/table-schema.json):

```java
JSONObject schemaJsonObj = new JSONObject();
Field nameField = new IntegerField("id");
schemaJsonObj.put("fields", new JSONArray());
schemaJsonObj.getJSONArray("fields").put(nameField.getJson());

Schema schema = Schema.fromJson(schemaJsonObj.toString(), true);

System.out.println(schema.isValid());
// true
```

## Setting Primary Key
### Single Key
```java
Schema schema = new Schema();

Field idField = new IntegerField("id");
schema.addField(idField);

Field nameField = new StringField("name");
schema.addField(nameField);

schema.setPrimaryKey("id");
String primaryKey = schema.getPrimaryKey();
```

### Composite Key
```java
Schema schema = new Schema();

Field idField = new IntegerField("id");
schema.addField(idField);

Field nameField = new StringField("name");
schema.addField(nameField);

Field surnameField = new StringField("surname");
schema.addField(surnameField);

schema.setPrimaryKey(new String[]{"name", "surname"});
String[] compositeKey = schema.getPrimaryKey();
```


## Casting
### Row Casting
To check if a given set of values complies with the schema, you can use `castRow`:

```java
Schema schema = new Schema();
        
// A String field.
Field stringField = new Field("stringField", Field.FIELD_TYPE_STRING);
schema.addField(stringField);

// An Integer field.
Field integerField = new Field("integerField", Field.FIELD_TYPE_INTEGER);
schema.addField(integerField);

// A Boolean field.
Field booleanField = new Field("booleanField", Field.FIELD_TYPE_BOOLEAN);
schema.addField(booleanField);

// Define a given set of values:
String[] row = new String[]{"John Doe", "25", "T"}

// Cast the row's values into their schema defined types: 
Object[] castRow = schema.castRow(row);
```

If a value in the given set of values cannot be cast to its expected type as defined by the schema, then an `InvalidCastException` is thrown.

### Field Casting
Data values can be cast to native Java objects with a Field instance. This allows formats and constraints to be defined for the field in the [field descriptor](https://specs.frictionlessdata.io/table-schema/#field-descriptors):

```java
Field intField = new Field("id", Field.FIELD_TYPE_INTEGER);
int intVal = intField.castValue("242");
System.out.print(intVal);

// 242

Field datetimeField = new Field("date", Field.FIELD_TYPE_DATETIME);
DateTime datetimeVal = datetimeField.castValue("2008-08-30T01:45:36.123Z");
System.out.print(datetimeVal.getYear());

// 2008

Field geopointField = new Field("coordinates", Field.FIELD_TYPE_GEOPOINT, Field.FIELD_FORMAT_ARRAY);
int[] geopointVal = geopointField.castValue("[12,21]");
System.out.print("lon: " + geopointVal[0] + ", lat: " + geopointVal[1]);

// lon: 12, lat: 21
```

Casting a value will check the value is of the expected type, is in the correct format, and complies with any constraints imposed in the descriptor.

Value that can't be cast will raise an `InvalidCastException`.

By default, casting a value that does not meet the constraints will raise a `ConstraintsException`.
Constraints can be ignored with by setting a boolean flag to false:

```java
// Define constraint limiting String length between 30 and 40 characters:
Map<String, Object> constraints = new HashMap();
constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 30);
constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 40);

// Cast a field and cast a value that violates the above constraint.
// Disable constrain enforcement by setting the enforceConstraints boolean flag to false.
Field field = new Field("name", Field.FIELD_TYPE_STRING, null, null, null, constraints);
field.castValue("This string length is greater than 45 characters.", false); // Setting false here ignores constraints during cast.

// ConstraintsException will not be thrown despite casting a value that does not meet the constraints.
```

You can call the `checkConstraintViolations` method to find out which constraints are being validated.
The method returns a map of violated constraints:

```java
Map<String, Object> constraints = new HashMap();
constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 5);
constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 15);

Field field = new Field("name", Field.FIELD_TYPE_INTEGER, null, null, null, constraints);

int constraintViolatingValue = 16;
Map<String, Object> violatedConstraints = field.checkConstraintViolations(constraintViolatingValue);

System.out.println(violatedConstraints);

// {maximum=15}
```

## Infer Type
The `Field` class' `castValue` used the `TypeInferrer` singleton to cast the given value into the desired type.
For instance, you can use the `TypeInferrer` singleton to cast a String representation of a number into a float like so:

```java
Map<String, Object> options = new HashMap();
options.put("bareNumber", false);
options.put("groupChar", " ");
options.put("decimalChar", ",");
float num = (float)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "1 564,123 EUR", options);    
```

## Contributing

Found a problem and would like to fix it? Have that great idea and would love to see it in the repository?

> Please open an issue before you start working.

It could save a lot of time for everyone and we are super happy to answer questions and help you along the way. Furthermore, feel free to join [frictionlessdata Gitter chat room](https://gitter.im/frictionlessdata/chat) and ask questions.

This project follows the [Open Knowledge International coding standards](https://github.com/okfn/coding-standards).

Get started:
```sh
# install jabba and maven2
$ cd tableschema-java
$ jabba install 1.8
$ jabba use 1.8
$ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
$ mvn test -B
```

Make sure all tests pass.
