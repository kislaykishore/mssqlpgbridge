/**
 * 
 */
package org.postgresql.jdbc;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.kishore.parser.CaseChangingCharStream;
import org.kishore.parser.MyTSqlParserVisitor;
import org.kishore.parser.TSqlLexer;
import org.kishore.parser.TSqlParser;
import org.kishore.parser.TSqlParser.Tsql_fileContext;

/**
 * @author kislay
 *
 */
public class SqlAdapter {

	public static String convertSql(final String sql) {
		try {
			CharStream stream = new CaseChangingCharStream(CharStreams.fromString(sql), true);
			TSqlLexer lexer = new TSqlLexer(stream);

			// Get a list of matched tokens
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			// Pass the tokens to the parser
			TSqlParser parser = new TSqlParser(tokens);
			// parser.setBuildParseTree(true);
			Tsql_fileContext file = parser.tsql_file();

			MyTSqlParserVisitor visitor = new MyTSqlParserVisitor();
			String str = visitor.visit(file);
			return str;
		} catch (Exception ex) {
			return sql;
		}
	}
}
