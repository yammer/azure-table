Azure Table [![Build Status](https://travis-ci.org/yammer/azure-table.png)](https://travis-ci.org/yammer/azure-table)

azure-table
===========

Azure based Guava Table implementation

There are four sub-modules:
- azure-table-core: provides the Guava Table adapter to Azure Table. Main class is `BaseAzureTable`, a table that uses the String object for row and column keys as well as values.
- azure-table-json: provides json serialization which allows for use of arbitrary java types for rows, columns, and values. Main class is `JsonSerializingTable`.
- azure-table-metrics: provides a metrics wrapper for the table. Main class is `MetredTable`.
- azure-table-util: combines all of the above, provides a fluent builder for the azure client and table.

azure-table-core
----------------
To start using the core library you simply need to include the following dependency:

    <dependency>
      <groupId>com.yammer.collections.azure</groupId>
      <artifactId>azure-table-core</artifactId>
      <version>${project.version}</version>
    </dependency>
    
This is a bare-bones integration. It requires you to provide the table name and an instance of `CloudTableClient` as provided by the Azure Java SDK: https://github.com/WindowsAzure/azure-sdk-for-java
It is your responsibility to ensure that the physical table exists beforehand. Also, no serialization mechanism is provided, row, columns and values need to be provided as `String` instances.

**IMPORTANT** Because the provided `BaseAzureTable` class is nothing but a view on a remote collection, in some aspects it breaks the guava `Table` interface. Namely, the *rowMap* and *columnMap* views,
don't behave like in-memory maps. For example, if you were to remove a row from the *rowMap* in the in-memory implementation you would expect to get the deleted row to be returned, but here this is not possible, 
as it is being physically deleted from the database. The only way to achieve such a behaviour would be to materialize (retrieve) the whole row in memory prior to deletion, however, 
given that Azure Table is meant to serve as a large distributed key-value store, such an approach is impractical.

**Testing**
This module contains both unit and integration tests. The latter are contained in `BaseAzureTableIT` and are run against an actual azure account that requires
credentials to be setup in a properties file located (you'll need to create the file) at the following path:

`azure-table-core/src/test/resources/com/yammer/collections/azure/azure-table-test-connection.properties`

The file should contain the following entries:

    account.name=<account name>
    account.key=<account key>

You can also choose not to run the integration tests. This can be achieved by passing the `-P noazure` option to your maven command line.

azure-table-json
----------------
To start using the json serialization library you simply need to include the following dependency.

    <dependency>
      <groupId>com.yammer.collections.azure</groupId>
      <artifactId>azure-table-core</artifactId>
      <version>${project.version}</version>
    </dependency>
    
This library provides a serialization layer which forms a bridge between guava `Table` instances that use arbitrary types for row, column and value objects and those that use only the `String` type.
The serialization layer is backed by jackson for automated json serialization and the yammer transforming collections library, which provides live read-write transforming views of other collections.
This functionality is provided via the `JsonSerializingTable`, which is a decorator around a `Table<String,String,String>` instance.

**IMPORTANT** You are strongly advised to use immutable data types when working with this library. As it is a view of a remote data store, mutable objects do not behave as they would have for in-memory collections.
First of all, using mutable values for the row and column keys is not a good idea in general. Second of all, when you get a value from the table and modify it, that modification
has to be explicitly pushed back to the table, by doing a put. This wouldn't be the case if it were a in-memory collection.

azure-table-metrics
-------------------
To start using the yammer metrics library integration you simply need to include the following dependency.

    <dependency>
      <groupId>com.yammer.collections.azure</groupId>
      <artifactId>azure-table-metrics</artifactId>
      <version>${project.version}</version>
    </dependency>
    
The key class is `MeteredTable` which is a decorotor around a `Table` instance.

This library provides metrics purely around the following operations: *get*, *put* and *remove*.

azure-table-util
----------------
To start using the util library you simply need to include the following dependency.

    <dependency>
      <groupId>com.yammer.collections.azure</groupId>
      <artifactId>azure-table-util</artifactId>
      <version>${project.version}</version>
    </dependency>

Provides fluent utility class for constructing `CloudTableClient` and an instance azure `Table`.

Examples:

1. Construct just the client:

```
    AzureTables.clientForAccount(accountName, accountKey).
               .withLinearRetryPolicy(retryInterval, numberOfAttempts). // optional
               .withTimeoutInMillis(timeout) // optional
               .build();
```

2. Get a table reference

```
    AzureTables.clientForAccount(accountName, accountKey)
               .tableWithName("tableName")
               .cloudTable();
```

3. Construct an azure table with metrics and using json serialization based on the configuration stored in `AzureTableConfiguration`, which can be loaded from a json or yaml file:

```
    AzureTables.clientForConfiguration(configuration)
               .createIfDoesNotExist()
               .andAddMetrics(). // optional
               .buildWithJsonSerialization(rowClass, columnClass, valueClass);
```

4. Construct an azure table with custom serialization, without metrics, and creating regardless:

```
    AzureTables.clientForAccount((accountName, accountKey)
               .tableWithName("tableName)
               .create()
               .buildUsingCustomSerialization(<serialization functions>);
```

5. Do something only if the table exists:

```
    Optional<AzureTables.clientForAccount((accountName, accountKey)
               .tableWithName("tableName)
               .create()
```
        
