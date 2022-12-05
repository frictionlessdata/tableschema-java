# Working with key contraints

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

