# SQL Server - PostgreSQL Bridge

This is a PostgresSQL driver which extends the PostgreSQL JDBC driver from https://jdbc.postgresql.org/ to handle SQL Server constructs. Basically, the intention here is to reduce the effort of migrating from SQL Server to PostgreSQL. We intend to cover as many cases as possible, but we know that there are certain limitations and not all MSSQL queries can directly be converted to PostgreSQL compatible ones.

The project builds upon Antlr grammars available here: https://github.com/antlr/grammars-v4/tree/master/tsql and traverses through the parse tree to generate the Postgres compatible SQL.

## Getting Started

* The test cases use [TestContainers](https://github.com/testcontainers/testcontainers-java) and therefore docker must be installed

## Building

* Run `./gradlew clean build -x signArchives` to compile the project, generate the jar, and run tests

## Releases

* The library releases can be downloaded from Maven Central. The coordinates are: io.github.kislaykishore:mssqlpgbridge:0.0.2

## Usage

* In order to load the driver, execute `Class.forName("mssqlpgbridge.driver.PgAdapterDriver");`
* JDBC URLs have similar construct as that of the Postgres JDBC driver, just that, instead of jdbc:postgresql://, here we have jdbc:mssqlpgbridge://
* Refer to the test cases for samples
 
## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
