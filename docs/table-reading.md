# reading tabular data

There are basically two distinct ways to read data with the help of the tableschema-java library:
- The way other language implementation of the Frictionless Tableschema standard handle this
- In a true native Java way

The first way is suitable in a mixed-technology enviroment where Java-specific solutions might create 
friction in maintenance, or in a big-data environment where modeling data as Java bean classes is 
not suitable

The Java-native way is superior if the modeling of data sets to Java classes is possible, as the user
of the library does not have to create Schemas and later convert the row data into Java objecs.

## Reading tabular data the Java way
Since Java is a strongly typed language, we can derive the data type for each column of a table from a
Java bean. This allows us to model our domain model in terms of Java beans, enjoy the format integrity
of a Table Schema, and while reading data, get it returned as instances of our bean class.

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
And with that, we can read the data as `SimpleDataBean` instances:
```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
            "/src/test/resources/fixtures/data/simple_data.csv");
// Load the data from URL without a schema.
Table simpleTable = Table.fromSource(url, (Schema)null, DataSourceFormat.getDefaultCsvFormat());

List<SimpleDataBean> data = new ArrayList<>();
Iterator<SimpleDataBean> bit = new BeanIterator<>(simpleTable, SimpleDataBean.class, false);
while (bit.hasNext()) {
    SimpleDataBean record = bit.next();
    data.add(record);
}

// 1 foo
// 2 bar
// 3 baz
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

### Reading tabular data using a Schema

To assure format integrity, we can create a Schema that describes the data we expect. 
Each row of the table will be returned as an Object array. Values in each column are parsed and 
converted to Java objects according to the field definition in the Schema. If data type and field
definition do not match, an exception would be thrown.

See https://specs.frictionlessdata.io/table-schema/ for more details on Schemas

```java
// Let's start by defining and building the schema of a table that contains data about employees:
Schema schema = new Schema();
schema.addField(new IntegerField("id"));
schema.addField(new StringField("title"));

URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
"/src/test/resources/fixtures/data/simple_data.csv");
// Load the data from URL with the schema.
Table table = Table.fromSource(url, schema, DataSourceFormat.getDefaultCsvFormat());

// Iterate through rows
Iterator<Object[]> iter = table.iterator();
while(iter.hasNext()){
    Object[] row = iter.next();
    System.out.println(Arrays.toString(row));
}

List<Object[]> allData = table.read();

// [1, foo]
// [2, bar]
// [3, baz]
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
Table table = Table.fromSource(url, schema, DataSourceFormat.getDefaultCsvFormat());

Iterator<Object[]> iter = table.iterator(false, false, true, false);
while(iter.hasNext()){

    // The fetched array will contain row values that have been cast into their
    // appropriate types as per field definitions in the schema.
    Object[] row = iter.next();

    BigInteger id = (BigInteger)row[0];
    String name = (String)row[1];
    LocalDate dob = (LocalDate)row[2];
    boolean isAdmin = (boolean)row[3];
    double[] addressCoordinates = (double[])row[4];
    Duration contractLength = (Duration)row[5];
    Map info = (Map)row[6];
}
```