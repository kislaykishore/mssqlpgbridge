/**
 * 
 */
package org.postgresql.jdbc;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import mssqlpgbridge.parser.CaseChangingCharStream;
import mssqlpgbridge.parser.CustomTSqlParserVisitor;
import mssqlpgbridge.parser.TSqlLexer;
import mssqlpgbridge.parser.TSqlParser;
import mssqlpgbridge.parser.ThrowingErrorListener;
import mssqlpgbridge.parser.TSqlParser.Tsql_fileContext;

/**
 * @author kislay
 *
 */
public class SqlConverter {

	public static String convertSql(final String sql) {
		try {
			CharStream stream = new CaseChangingCharStream(CharStreams.fromString(sql), true);
			TSqlLexer lexer = new TSqlLexer(stream);

			// Get a list of matched tokens
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			// Pass the tokens to the parser
			TSqlParser parser = new TSqlParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(ThrowingErrorListener.INSTANCE);
			// parser.setBuildParseTree(true);
			Tsql_fileContext file = parser.tsql_file();

			CustomTSqlParserVisitor visitor = new CustomTSqlParserVisitor();
			String str = visitor.visit(file);
			return str;
		} catch (Exception ex) {
			return sql;
		}
	}
}
