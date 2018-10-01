# SQL Server - PostgreSQL Bridge

This is a PostgresSQL driver which extends the PostgreSQL JDBC driver from https://jdbc.postgresql.org/ to handle SQL Server constructs

## Getting Started

* Install a PostgreSQL instance where you can run the test cases
* Create a user who has sufficient permissions to run DDL and DML queries

## Building

* Pass username and passwords as system properties
* Set postgres.username, postgres.password, postgres.dbname JVM arguments in build.gradle with postgres username, password and database name respectively
* Run `./gradlew clean build` to run compile the project, generate the jar, and run tests

## Usage

* To load the driver, execute `Class.forName("mssqlpgbridge.driver.PgAdapterDriver");`
* JDBC URLs have similar construct as that of the Postgres JDBC driver, just that, instead of jdbc:postgresql://, here we have jdbc:mssqlpgbridge://
 
## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details