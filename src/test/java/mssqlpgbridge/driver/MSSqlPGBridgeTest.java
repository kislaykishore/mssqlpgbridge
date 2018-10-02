/**
 * 
 */
package mssqlpgbridge.driver;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @author kislay
 *
 */
public class MSSqlPGBridgeTest {

	private static final String TEST_DB = "testdb";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@ClassRule
	public static PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:alpine")
			.withDatabaseName(TEST_DB);

	@Test
	public void testTempTableAccess() throws Exception {
		String sql = "CREATE TABLE #abcd(a int);INSERT INTO #abcd(a) values(1);INSERT INTO #abcd(a) values(2);";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM #abcd")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(1));
				rs.next();
				int v2 = rs.getInt(1);
				assertThat(v2, equalTo(2));
			}
			stmt.executeUpdate("DROP TABLE #abcd");
		}
	}

	@Test
	public void testUnloggedTableAccess() throws Exception {
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			String setup = "DROP TABLE IF EXISTS abcd";
			stmt.executeUpdate(setup);
		}
		String sql = "CREATE TABLE ##abcd(a int);INSERT INTO ##abcd(a) values(1);INSERT INTO ##abcd(a) values(2);";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM ##abcd")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(1));
				rs.next();
				int v2 = rs.getInt(1);
				assertThat(v2, equalTo(2));
			}
			stmt.executeUpdate("DROP TABLE abcd");
		}
	}

	@Test
	public void testNoLockRemove() throws Exception {
		String sql = "CREATE TABLE #abcd(a int);INSERT INTO #abcd(a) values(1);INSERT INTO #abcd(a) values(2)";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM #abcd WITH(NOLOCK) WHERE a=1")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(1));
			}
			stmt.executeUpdate("DROP TABLE #abcd");
		}
	}

	@Test
	public void testWithIndexRemove() throws Exception {
		String sql = "CREATE TABLE #abcd(a int);INSERT INTO #abcd(a) values(1);INSERT INTO #abcd(a) values(2)";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM #abcd WITH (INDEX(PK_Contact_ContactID)) WHERE a=1")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(1));
			}
			stmt.executeUpdate("DROP TABLE #abcd");
		}
	}

	@Test
	public void testSyntaxError() throws Exception {
		thrown.expect(SQLException.class);
		String sql = "CREATE TABLE #abcd(a int);SELECT TOP 10 * FROM #abcd;";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);

		}
	}

	@Test
	public void testNoLockWithoutWithRemove() throws Exception {
		String sql = "CREATE TABLE #abcd(a int);INSERT INTO #abcd(a) values(1);INSERT INTO #abcd(a) values(2)";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM #abcd (NOLOCK) WHERE a=1")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(1));
			}
			stmt.executeUpdate("DROP TABLE #abcd");
		}
	}

	@Test
	public void testIsNullHandling() throws Exception {
		String sql = "CREATE TABLE #pqrs(a int);INSERT INTO #pqrs(a) values(NULL);INSERT INTO #pqrs(a) values(2)";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT ISNULL(a, -1) FROM #pqrs (NOLOCK)")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(-1));
				rs.next();
				int v2 = rs.getInt(1);
				assertThat(v2, equalTo(2));
			}
			stmt.executeUpdate("DROP TABLE #pqrs");
		}
	}

	@Test
	public void testStatementTermination() throws Exception {
		// Postgres needs statements in a batch to be separated by semi-colon but as
		// shown in the example below, it is being handled
		String sql = "CREATE TABLE #pqrs(a int) INSERT INTO #pqrs(a) values(NULL)  INSERT INTO #pqrs(a) values(2)";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
			try (ResultSet rs = stmt.executeQuery("SELECT ISNULL(a, -1) FROM #pqrs (NOLOCK)")) {
				rs.next();
				int v1 = rs.getInt(1);
				assertThat(v1, equalTo(-1));
				rs.next();
				int v2 = rs.getInt(1);
				assertThat(v2, equalTo(2));
			}
			stmt.executeUpdate("DROP TABLE #pqrs");
		}
	}

	@Test
	public void testConnection() throws Exception {
		String SQL = "SELECT 1;";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(SQL)) {
			rs.next();
			rs.getInt(1);
		}
	}

	private Connection connect() throws SQLException, ClassNotFoundException {
		Class.forName("mssqlpgbridge.driver.PgAdapterDriver");
		String jdbcUrl = POSTGRES_CONTAINER.getJdbcUrl();
		jdbcUrl = jdbcUrl.replace("postgresql", "mssqlpgbridge");
		String username = POSTGRES_CONTAINER.getUsername();
		String password = POSTGRES_CONTAINER.getPassword();
		return DriverManager.getConnection(jdbcUrl, username, password);

	}
}
