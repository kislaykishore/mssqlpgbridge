/**
 * 
 */
package mssqlpgbridge.driver;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

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

	@SuppressWarnings("rawtypes")
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
	public void testGetDate() throws Exception {
		String sql1 = "SELECT GETDATE()";
		String sql2 = "SELECT GETDATE() as dt";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql1)) {
			rs.next();
			Date dt = rs.getDate(1);
			assertThat(dt, is(not(equalTo(null))));
		}

		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql2)) {
			rs.next();
			Date dt = rs.getDate("dt");
			assertThat(dt, is(not(equalTo(null))));
		}
	}

	@Test
	public void testGetUTCDate() throws Exception {
		String sql1 = "SELECT GETUTCDATE()";
		String sql2 = "SELECT GETUTCDATE() as dt";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql1)) {
			rs.next();
			Date dt = rs.getDate(1);
			assertThat(dt, is(not(equalTo(null))));
		}

		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql2)) {
			rs.next();
			Date dt = rs.getDate("dt");
			assertThat(dt, is(not(equalTo(null))));
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
	public void testDateDiffYear() throws Exception {
		String sql = "SELECT DATEDIFF(year, '2011-10-02', '2012-01-01');";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(1));
		}
	}
	
	@Test
	public void testDateDiffMonth() throws Exception {
		String sql = "SELECT DATEDIFF(month, '2011-10-02', '2012-01-01');";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(3));
		}
	}
	
	@Test
	public void testDateDiffWeek() throws Exception {
		String sql = "SELECT DATEDIFF(week, '2011-12-22', '2011-12-31');";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(1));
		}
	}
	
	@Test
	public void testDateDiffDay() throws Exception {
		String sql = "SELECT DATEDIFF(day, '2011-12-29 23:00:00', '2011-12-31 01:00:00')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(2));
		}
	}
	
	@Test
	public void testDateDiffDay_2() throws Exception {
		String sql = "SELECT DATEDIFF(day, '2011-12-29 23:00:00', '2011-12-31 23:00:00')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(2));
		}
	}
	
	@Test
	public void testDateDiffDay_3() throws Exception {
		String sql = "SELECT DATEDIFF(day, '2011-12-29 23:00:00', '2011-12-31 23:00:01')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(3));
		}
	}
	
	@Test
	public void testDateDiffDay_4() throws Exception {
		String sql = "SELECT DATEDIFF(day, '2011-12-31 23:00:01', '2011-12-29 23:00:00')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(-3));
		}
	}
	
	@Test
	public void testDateDiffHour() throws Exception {
		String sql = "SELECT DATEDIFF(hour, '2011-12-30 08:55', '2011-12-30 09:55')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(1));
		}
	}
	
	@Test
	public void testDateDiffSecond() throws Exception {
		String sql = "SELECT DATEDIFF(second, '2011-12-30 08:54:55', '2011-12-30 08:56:10')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(75));
		}
	}
	
	
	@Test
	public void testDateDiffDay_5() throws Exception {
		String sql = "SELECT DATEDIFF(day, '2011-12-31 23:00:00', '2011-12-29 23:00:00')";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			rs.next();
			int v1 = rs.getInt(1);
			assertThat(v1, equalTo(-2));
		}
	}
	
	@Test
	public void testDateAddDay() throws Exception {
		String sql = "SELECT DATEADD(day, 1, GETDATE())";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			rs.next();
			Date v1 = rs.getDate(1);
			assertThat(v1, not(equalTo(null)));
			Date dt = Date.valueOf(LocalDate.now().plusDays(1));
			assertThat(v1, equalTo(dt));
		}
	}
	
	@Test
	public void testDateAddYear() throws Exception {
		String sql = "SELECT DATEADD(year, -1, GETDATE())";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			rs.next();
			Date v1 = rs.getDate(1);
			assertThat(v1, not(equalTo(null)));
			Date dt = Date.valueOf(LocalDate.now().plusYears(-1));
			assertThat(v1, equalTo(dt));
		}
	}
	
	@Test
	public void testDateAddMonths() throws Exception {
		String sql = "SELECT DATEADD(month, -1, GETDATE())";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			rs.next();
			Date v1 = rs.getDate(1);
			assertThat(v1, not(equalTo(null)));
			Date dt = Date.valueOf(LocalDate.now().plusMonths(-1));
			assertThat(v1, equalTo(dt));
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
