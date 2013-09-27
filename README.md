azure-table
===========

Azure based Guava Table implementation


WARNING: this package is in very early stages of development. The current implementation is a MVP required by the Secretie core service. This service deals with small data sets and performance/scalability are not a priority.
For that reason only handful of the Guava Table methods are implemented, not all of them satisfy the exact cotract, and importantly, the implementation is not shy to load all the data to memmory.

There are two sub-modules:
- azure-table-core: provides the Guava Table adapter to Azure Table. It consists of two main classes:
  - StringAzureTable, a table that uses the String object for row and column keys as well as values.
  - AzureTable, a generic implementation that uses Type to String marshallers and is backed by the StringAzureTable
- azure-table-backup: a backup library and a command line tool that operates on guava table and is integrated with azure. Because azure table is non-transactional, this tool requires external synchronisation and retry logic. In esures only that there are is no false success to a operation.
