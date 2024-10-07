# Google Spanner driver adapter
The Google Cloud Spanner driver adapter is a NoSQLBench adapter for the `gcp_spanner` driver, a Java driver
for connecting to and performing operations on an instance of a Google Cloud Spanner database.

## Activity Parameters

The following parameters must be supplied to the adapter at runtime in order to successfully connect to an
instance of the [Google Cloud Spanner database](https://cloud.google.com/java/docs/reference/google-cloud-spanner/latest/overview):

* `service_account_file` - In order to connect to a Spanner database you must have a [IAM service account](https://cloud.google.com/docs/authentication/provide-credentials-adc#service-account)
defined with the appropriate permissions associated with the adapter. Once the service account is created you can download
a file from the gcp console in JSON format that contains the credentials for the service account. This file must be provided
to the adapter at runtime.
* `project_id` - Project ID containing the Spanner database. See [Creating a project](https://cloud.google.com/resource-manager/docs/creating-managing-projects).
* `instance_id` - Spanner database's Instance ID. See [Creating an instance](https://cloud.google.com/spanner/docs/getting-started/java#create_an_instance).
* `database_id` - Spanner database's Database ID. See [Creating a database](https://cloud.google.com/spanner/docs/getting-started/java#create_a_database).
* In addition to this the environment variable `GOOGLE_APPLICATION_CREDENTIALS` must be set to the path of the service account file.

## Op Templates

The Google Cloud Spanner adapter supports the following operations:

* `create_database_ddl` - Data Definition Language operation for creating a database.
* `update_database_ddl` - Data Definition Language operations such as creating and dropping tables, indexes, etc.
* `execute_dml` - Data Manipulation Language operations. Read only operations are supported at this time, including queries
and vector queries.
* `insert` - Insert a single record, vector or non-vector, of data into the database.
* `drop_database_ddl` - Data Definition Language operation for dropping a database.

## Examples

Checkout the sample workload files [here](./activities).
---
