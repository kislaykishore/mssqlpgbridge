/**
 * 
 */
package org.postgresql.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.postgresql.util.HostSpec;

/**
 * @author kislay
 *
 */
public class PgAdapterConnection extends PgConnection {

	public PgAdapterConnection(HostSpec[] hostSpecs, String user, String database, Properties info, String url)
			throws SQLException {
		super(hostSpecs, user, database, info, url);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		checkClosed();
		return new PgAdapterStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		String modifiedSql = SqlConverter.convertSql(sql);
		checkClosed();
		return new PgPreparedStatement(this, modifiedSql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

}
