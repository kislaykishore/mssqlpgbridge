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
	
	public PgAdapterStatement(PgConnection c, int rsType, int rsConcurrency, int rsHoldability) throws SQLException {
		super(c, rsType, rsConcurrency, rsHoldability);
	}
	
	public boolean executeWithFlags(String sql, int flags) throws SQLException {
		String modifiedSql = SqlAdapter.convertSql(sql);
	    return super.executeWithFlags(modifiedSql, flags);
	  }

	
}
