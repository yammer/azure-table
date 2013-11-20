azure-table
===========

Azure based Guava Table implementation

There are five sub-modules:
- azure-table-core: provides the Guava Table adapter to Azure Table. Main classe is `BaseAzureTable`, a table that uses the String object for row and column keys as well as values.
- azure-table-json: provides json serialization which allows for use of arbitrary java types for rows, columns, and values. Main class is `JsonSerializingTable`.
- azure-table-metrics: provides a metrics wrapper for the table. Main class is `MetredTable`.
- azure-table-util: combines all of the above, provides a fluent builder for the azure client and table.
- azure-table-backup: a backup library and a command line tool that operates on guava table and is integrated with azure. Because azure table is non-transactional, this tool requires external synchronisation and retry logic. In esures only that there are is no false success to a operation.

azure-table-core
----------------
To start using the core library you simply need to include the following dependency:

    <dependency>
      <groupId>com.yammer.collections.azure</groupId>
      <artifactId>azure-table-core</artifactId>
      <version>1.1.3</version>
    </dependency>
    
This is a bare-bones integration. It requires you to provide the table name and an instance of `CloudTableClient` as provided by the Azure Java SDK: https://github.com/WindowsAzure/azure-sdk-for-java
It is your responsibility to ensure that the physical table exists beforhand. Also, no serialization mechanism is provided, row, columns and values need to be provided as `String` instances.

**IMPORTANT** because the provided `BaseAzureTable` class is nothing but a view on a remote collection, in some aspects it breaks the guava `Table` interface. Namely, the *rowMap* and *columnMap* views,
don't behave like in-memmory maps. For example, if you were to remove a row from the *rowMap* in the in-memmory implementation you would expect to get the deleted row to be returned, but here this is not possible, 
as it is being physically deleted from the database. The only way to achieve such a behaviour would be to materialize (retrive) the whole row in memmory prior to deletion, however, 
given that Azure Table is meant to serve as a large distributed key-value store, such an approach is impractical.

azure-table-json
----------------
To start using the json serialization library you simply need to include the following dependency.

    <dependency>
      <groupId>com.yammer.collections.azure</groupId>
      <artifactId>azure-table-core</artifactId>
      <version>1.1.3</version>
    </dependency>
    
This library provides an automated serialization layer between     
