/**
 * 
 */
package mssqlpgbridge.mybatis;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.Reader;
import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import mybatis.Student;

/**
 * @author kislay
 *
 */
public class MyBatisTest {

	private SqlSession session = null;
	private static final String TEST_DB = "testdb";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:alpine")
			.withDatabaseName(TEST_DB);

	public MyBatisTest() throws IOException {
	}

	@Before
	public void setUp() throws IOException {
		try (Reader reader = Resources.getResourceAsReader("sqlmap.xml")) {

			String jdbcUrl = POSTGRES_CONTAINER.getJdbcUrl();
			jdbcUrl = jdbcUrl.replace("postgresql", "mssqlpgbridge");
			String username = POSTGRES_CONTAINER.getUsername();
			String password = POSTGRES_CONTAINER.getPassword();
                        HikariConfig cfg = new HikariConfig();
                        cfg.setUsername(username);
                        cfg.setPassword(password);
                        cfg.setJdbcUrl(jdbcUrl);
			cfg.setDriverClassName("mssqlpgbridge.driver.PgAdapterDriver");

			DataSource ds = new HikariDataSource(cfg);
			TransactionFactory transactionFactory = new JdbcTransactionFactory();
			Environment environment = new Environment("development", transactionFactory, ds);
			Configuration config = new Configuration(environment);
			config.addMapper(Student.class);

			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
			this.session = sqlSessionFactory.openSession();
		}
	}

	@Test
	public void helloWorld() {
		Integer val = this.session.selectOne("fetchHello");
		assertThat(val, equalTo(1));
	}
	
	@Test
	public void testParams() {
		String str = this.session.selectOne("paramsFetch", 12);
		assertThat(str, equalTo("hello"));
	}

	@Test
	public void testTempTable() {
		List<Integer> list = this.session.selectList("fetchValues");
		Set<Integer> st = new HashSet<>(list);
		assertThat(st.size(), equalTo(2));
		assertThat(st.contains(1), equalTo(true));
		assertThat(st.contains(2), equalTo(true));
	}

}
