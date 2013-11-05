azure-table
===========

Azure based Guava Table implementation

There are five sub-modules:
- azure-table-core: provides the Guava Table adapter to Azure Table. Main classe is *BaseAzureTable*, a table that uses the String object for row and column keys as well as values.
- azure-table-json: provides json serialization which allows for use of arbitrary java types for rows, columns, and values. Main class is *JsonSerializingTable*.
- azure-table-metrics: provides a metrics wrapper for the table. Main class is MetredTable
- azure-table-util: combines all of the above, provides a fluent builder for the azure client and table.
- azure-table-backup: a backup library and a command line tool that operates on guava table and is integrated with azure. Because azure table is non-transactional, this tool requires external synchronisation and retry logic. In esures only that there are is no false success to a operation.
