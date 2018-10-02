/**
 * 
 */
package org.postgresql.jdbc;

import java.sql.SQLException;

/**
 * @author kislay
 *
 */
public class PgAdapterStatement extends PgStatement {

	private static final String SYNTAX_ERROR_MESSAGE = "syntax error";

	public PgAdapterStatement(PgConnection c, int rsType, int rsConcurrency, int rsHoldability) throws SQLException {
		super(c, rsType, rsConcurrency, rsHoldability);
	}

	public boolean executeWithFlags(String sql, int flags) throws SQLException {
		try {
			String modifiedSql = SqlConverter.convertSql(sql);
			return super.executeWithFlags(modifiedSql, flags);
		} catch (SQLException ex) {
			// In case of any errors, try out with the original sql to see if that works
			if (ex.getMessage().contains(SYNTAX_ERROR_MESSAGE)) {
				return super.executeWithFlags(sql, flags);
			} else {
				throw ex;
			}
		}
	}

}
