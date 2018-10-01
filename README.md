# SQL Server - PostgreSQL Bridge

This is a PostgresSQL driver which extends the PostgreSQL JDBC driver from https://jdbc.postgresql.org/ to handle SQL Server constructs. Basically, the intention here is to reduce the effort of migrating from SQL Server to PostgreSQL. We intend to cover as many cases as possible, but we know that there are certain limitations and not all MSSQL queries can directly be converted to PostgreSQL compatible ones.

The project builds upon tokenizer and parser Antlr grammars available here: https://github.com/antlr/grammars-v4/tree/master/tsql and traverses through the parse tree to generate the Postgres compatible SQL.

## Getting Started

* Install a PostgreSQL instance where you can run the test cases
* Create a user who has sufficient permissions to run DDL and DML queries

## Building

* Pass username and passwords as system properties
* Set postgres.username, postgres.password, postgres.dbname JVM arguments in build.gradle with postgres username, password and database name respectively
* Run `./gradlew clean build` to compile the project, generate the jar, and run tests

## Usage

* To load the driver, execute `Class.forName("mssqlpgbridge.driver.PgAdapterDriver");`
* JDBC URLs have similar construct as that of the Postgres JDBC driver, just that, instead of jdbc:postgresql://, here we have jdbc:mssqlpgbridge://
* Refer to the test cases for samples

 
## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
