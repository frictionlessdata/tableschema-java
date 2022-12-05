# Reading tabular data

You can write a `Table` into a CSV file:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
Table table = Table.fromSource(url);
table.write("/path/to/write/table.csv");
```