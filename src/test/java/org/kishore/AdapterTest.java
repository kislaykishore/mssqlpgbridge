/**
 * 
 */
package org.kishore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author kislay
 *
 */
public class AdapterTest {

	@BeforeClass
	public static void setup() throws ClassNotFoundException, SQLException {
		connect();
	}
	
	@Test
	public void test() throws Exception {
		String sql = "CREATE TABLE #abcd(a int);INSERT INTO #abcd(a) values(1);INSERT INTO #abcd(a) values(2);";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement()){
			stmt.executeUpdate(sql);
			try(ResultSet rs = stmt.executeQuery("SELECT * FROM #abcd")) {
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
	public void testConnection() throws Exception {
		String SQL = "SELECT 1;";
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(SQL)) {
			rs.next();
			rs.getInt(1);
		}
	}

	private static Connection connect() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.PgAdapterDriver");
		String url = "jdbc:postgreadp://localhost/testdb";
		String user = "testuser";
		String password = "test1234";
		return DriverManager.getConnection(url, user, password);
	}
}
