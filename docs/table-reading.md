# Reading tabular data

- [Reading data the Java way](#reading-tabular-data-the-java-way) explains various ways of reading data
       as instances of a provided Bean class
  - [Reading from CSV/JSON data](#reading-from-CSV/JSON-data) Read data and transparently convert them
       to Java Beans.
  - [Creating a Table from Java Bean Objects](#creating-a-table-from-java-bean-objects) Construct a Table
       on a Collection of Java Beans.
- [Reading data the Frictionless way](#reading-tabular-data-the-frictionless-way) - either as String Arrays, 
       Object Arrays, or Maps
  - [Without a Schema](#reading-tabular-data-without-a-schema) Read as String arrays and without
        format integrity assurance
  - [With a Schema](#reading-tabular-data-using-a-schema) Read as converted Java object arrays with
        format integrity assurance
  - [Read data as Maps](#reading-tabular-data-returning-rows-as-maps) if you prefer your data as
        key/value pairs

There are basically two distinct ways to read data with the help of the tableschema-java library:
- The way other language implementation of the Frictionless Tableschema standard handle this
- In a true native Java way

The first way is suitable in a mixed-technology enviroment where Java-specific solutions might create 
friction in maintenance, or in a big-data environment where modeling data as Java bean classes is 
not suitable

The Java-native way is superior if the modeling of data sets to Java classes is possible, as the user
of the library does not have to create Schemas and later convert the row data into Java objecs. It also 
allows very precise control over the mapping between CSV/JSON and domain objects.

## Reading tabular data the Java way
Since Java is a strongly typed language, we can derive the data type for each column of a table from a
Java bean. This allows us to model our domain model in terms of Java beans, enjoy the format integrity
of a Table Schema, and while reading data, get it returned as instances of our bean class.

Some limitations apply: the Bean serialization/deserialization does not know all the bells and whistles
that e.g. Jackson does and you can't use nested data - after all, tabular data is not hierarchical and
is not well suited for object trees. Also, you can't use custom classes for your members, you need 
to stick to a pre-defined list of classes.

First we define a Bean class:
```java
public class SimpleDataBean {

    private Integer id;

    private String title;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return id + " " + title;
    }
}
```
### Reading from CSV/JSON data
And with that, we can read the data as `SimpleDataBean` instances:
```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
            "/src/test/resources/fixtures/data/simple_data.csv");
// Load the data from URL without a schema.
Table table = Table.fromSource(url, (Schema)null, DataSourceFormat.getDefaultCsvFormat(), StandardCharsets.UTF_8);

List<SimpleDataBean> data = new ArrayList<>();
Iterator<SimpleDataBean> bit = new BeanIterator<>(table, SimpleDataBean.class, false);
while (bit.hasNext()) {
    SimpleDataBean record = bit.next();
    data.add(record);
}

// 1 foo
// 2 bar
// 3 baz
```

### Creating a Table from Java Bean Objects

If we have a Collection of our business objects as data, we can construct a Table with the data as backing. 
All the iterators from the other examples are available for reading the serialized data - or a Data Package
could be written.

You can export a synthesized Schema based on the Bean class. It is a regular Table Schema.

```java
List<EmployeeBeanWithAnnotation> employees = new ArrayList<>();

Table t = new Table(employees, EmployeeBeanWithAnnotation.class);

Schema schema = t.getSchema();
Iterator<Object[]> iter = table.iterator();
while(iter.hasNext()){
    Object[] row = iter.next();
    System.out.println(Arrays.toString(row));
}
```


## Reading tabular data the Frictionless way

### Reading tabular data without a Schema

This is the simplest case where we read data from a file or URL. Each row of the table will be returned as
as an Object array. Values in each column are parsed and converted to Java objects on a best guess approach.

Cast [data](https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv) from a CSV without a schema:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                "/src/test/resources/fixtures/data/simple_data.csv");
Table table = Table.fromSource(url);

// Iterate through rows
Iterator<Object[]> iter = table.iterator();
while(iter.hasNext()){
    Object[] row = iter.next();
    System.out.println(Arrays.toString(row));
}

// [1, foo]
// [2, bar]
// [3, baz]

// Read the entire CSV and output it as a List:
List<Object[]> allData = table.read();
```


### Reading tabular data returning rows as Maps

The Table object has flexible iterators that can return table rows as `Map<String, Object>` instead
of `Object`. In this case, the column header is the key and the row value is the value in the map:

```java
Schema schema = new Schema();

schema.addField(new IntegerField("id"));
schema.addField(new StringField("title"));
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
"/src/test/resources/fixtures/data/simple_data.csv");
Table table = Table.fromSource(url);

// Iterate through rows
Iterator<Map<String, Object>> iter = table.keyedIterator();
while(iter.hasNext()){
    Map<String, Object> row = iter.next();
    System.out.println(row);
}

// {id=1, title=foo}
// {id=2, title=bar}
// {id=3, title=baz}
```


### Parse a CSV with a Schema, extended version

If you have a schema, you can input it as parameter when creating the `Table` instance so that the [data](https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/employee_data.csv) from the CSV will be cast into the field types defined in the schema:

```java
// Let's start by defining and building the schema of a table that contains data on employees:
Schema schema = new Schema();

Field idField = new IntegerField("id");
schema.addField(idField);

Field nameField = new StringField("name");
schema.addField(nameField);

Field dobField = new DateField("dateOfBirth");
schema.addField(dobField);

Field isAdminField = new BooleanField("isAdmin");
schema.addField(isAdminField);

Field addressCoordinatesField = new GeopointField("addressCoordinates");
addressCoordinatesField.setFormat(Field.FIELD_FORMAT_OBJECT);
schema.addField(addressCoordinatesField);

Field contractLengthField = new DurationField("contractLength");
schema.addField(contractLengthField);

Field infoField = new ObjectField("info");
schema.addField(infoField);

// Load the data from URL with the schema.
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
        "/src/test/resources/fixtures/data/employee_data.csv");
Table table = Table.fromSource(url, schema, DataSourceFormat.getDefaultCsvFormat(), , StandardCharsets.UTF_8);

Iterator<Object> iter = table.iterator(false, false, true, false);
while(iter.hasNext()){

    // The fetched array will contain row values that have been cast into their
    // appropriate types as per field definitions in the schema.
    Object[] row = (Object[])iter.next();

    BigInteger id = (BigInteger)row[0];
    String name = (String)row[1];
    LocalDate dob = (LocalDate)row[2];
    boolean isAdmin = (boolean)row[3];
    double[] addressCoordinates = (double[])row[4];
    Duration contractLength = (Duration)row[5];
    Map info = (Map)row[6];
}
```
