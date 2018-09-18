package org.kishore.parser;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class MyTSqlParserVisitor extends TSqlParserBaseVisitor<String>{

	@Override
	public String visitTerminal(TerminalNode node) {
		int type = node.getSymbol().getType();
		switch(type) {
		case Token.EOF:
			return "";
		default:
			String val = node.getText();
		    return val == null?"":" " + val;
		}
	}
	
	public String rename(RuleNode node, String newName) {
		String result = defaultResult();
		int n = node.getChildCount();
		for (int i=1; i<n; i++) {
			if (!shouldVisitNextChild(node, result)) {
				break;
			}

			ParseTree c = node.getChild(i);
			String childResult = c.accept(this);
			result = aggregateResult(result, childResult);
		}
		return " " + newName + result;
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
	
	@Override
	public String visitISNULL(TSqlParser.ISNULLContext ctx) { 
		return rename(ctx, "COALESCE");
	}
	
	@Override
	public String visitWith_table_hints(TSqlParser.With_table_hintsContext ctx) {
		return "";
	}
	
}
