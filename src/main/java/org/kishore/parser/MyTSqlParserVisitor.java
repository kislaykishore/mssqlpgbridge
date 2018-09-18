package org.kishore.parser;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class MyTSqlParserVisitor extends TSqlParserBaseVisitor<String>{

	@Override
	public String visitTerminal(TerminalNode node) {
		if(node.getSymbol().getType() == Token.EOF) {
			return "";
		}
		String val = node.getText();
		return val == null?"":" " + val;
	}
	
	@Override
	public String aggregateResult(String aggregate, String nextResult) {
		StringBuilder sb = new StringBuilder();
		if(aggregate != null) {
			sb.append(aggregate);
		}
		if(nextResult != null) {
			sb.append(nextResult);
		}
		return sb.toString();
	}
	
	@Override
	public String visitSql_clause(TSqlParser.Sql_clauseContext ctx) {
		String text = visitChildren(ctx);
		if(text != null && !text.endsWith(";")) {
			return text + ";";
		}
		return text;
	}
}
