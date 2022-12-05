# Casting
## Row Casting
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

## Field Casting
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

## Checking constraints
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

## Infering a Field type
The `Field` class' `castValue` used the `TypeInferrer` singleton to cast the given value into the desired type.
For instance, you can use the `TypeInferrer` singleton to cast a String representation of a number into a float like so:

```java
Map<String, Object> options = new HashMap();
options.put("bareNumber", false);
options.put("groupChar", " ");
options.put("decimalChar", ",");
float num = (float)TypeInferrer.getInstance().castValue(Field.FIELD_FORMAT_DEFAULT, "1 564,123 EUR", options);    
```
