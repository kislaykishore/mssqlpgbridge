package mssqlpgbridge.parser;

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
	
	public String skipNChildren(RuleNode node, int k) {
		String result = defaultResult();
		int n = node.getChildCount();
		for (int i=k; i<n; i++) {
			if (!shouldVisitNextChild(node, result)) {
				break;
			}

			ParseTree c = node.getChild(i);
			String childResult = c.accept(this);
			result = aggregateResult(result, childResult);
		}
		return " " + result;
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
		return " COALESCE " + skipNChildren(ctx, 1);
	}
	
	@Override
	public String visitWith_table_hints(TSqlParser.With_table_hintsContext ctx) {
		return "";
	}
	
	@Override
	public String visitCreate_table(TSqlParser.Create_tableContext ctx) {
		String value = skipNChildren(ctx, 2).trim();
		if(value.startsWith("##")) {
			return "CREATE UNLOGGED TABLE " + value.substring(2);
		} else if(value.startsWith("#")) {
			return "CREATE TEMP TABLE " + value.substring(1);
		}
		return "CREATE TABLE " + value;
	}
	
	@Override
	public String visitTable_name(TSqlParser.Table_nameContext ctx) {
		if(ctx.parent instanceof TSqlParser.Create_tableContext) {
			return ctx.getText();
		}
		String val = visitChildren(ctx).trim();
		if(val.startsWith("##")) {
			val = val.substring(2);
		} else if(val.startsWith("#")) {
			val = val.substring(1);
		}
		return " " + val;
	}
	
	@Override
	public String visitFull_table_name(TSqlParser.Full_table_nameContext ctx) {
		if(ctx.parent instanceof TSqlParser.Create_tableContext) {
			return ctx.getText();
		}
		String val = visitChildren(ctx).trim();
		if(val.startsWith("##")) {
			val = val.substring(2);
		} else if(val.startsWith("#")) {
			val = val.substring(1);
		}
		return " " + val;
	}
	
	
}
