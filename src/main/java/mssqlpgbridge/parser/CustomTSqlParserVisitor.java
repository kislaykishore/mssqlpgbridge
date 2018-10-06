package mssqlpgbridge.parser;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CustomTSqlParserVisitor extends TSqlParserBaseVisitor<String> {

	@Override
	public String visitTerminal(TerminalNode node) {
		int type = node.getSymbol().getType();
		switch (type) {
		case Token.EOF:
			return "";

		default:
			String val = node.getText();
			return val == null ? "" : " " + val;
		}
	}

	@Override
	public String aggregateResult(String aggregate, String nextResult) {
		StringBuilder sb = new StringBuilder();
		if (aggregate != null) {
			sb.append(aggregate);
		}
		if (nextResult != null) {
			sb.append(nextResult);
		}
		return sb.toString();
	}

	@Override
	public String visitGETDATE(TSqlParser.GETDATEContext ctx) {
		return " localtimestamp";
	}

	@Override
	public String visitGETUTCDATE(TSqlParser.GETUTCDATEContext ctx) {
		return " current_timestamp at time zone 'utc'";
	}

	@Override
	public String visitSql_clause(TSqlParser.Sql_clauseContext ctx) {
		String text = visitChildren(ctx);
		if (text != null && !text.endsWith(";")) {
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
		if (value.startsWith("##")) {
			return "CREATE UNLOGGED TABLE " + value.substring(2);
		} else if (value.startsWith("#")) {
			return "CREATE TEMP TABLE " + value.substring(1);
		}
		return "CREATE TABLE " + value;
	}

	@Override
	public String visitTable_name(TSqlParser.Table_nameContext ctx) {
		if (ctx.parent instanceof TSqlParser.Create_tableContext) {
			return ctx.getText();
		}
		String val = visitChildren(ctx).trim();
		if (val.startsWith("##")) {
			val = val.substring(2);
		} else if (val.startsWith("#")) {
			val = val.substring(1);
		}
		return " " + val;
	}

	@Override
	public String visitFull_table_name(TSqlParser.Full_table_nameContext ctx) {
		if (ctx.parent instanceof TSqlParser.Create_tableContext) {
			return ctx.getText();
		}
		String val = visitChildren(ctx).trim();
		if (val.startsWith("##")) {
			val = val.substring(2);
		} else if (val.startsWith("#")) {
			val = val.substring(1);
		}
		return " " + val;
	}
	
	@Override
	public String visitDATEADD(TSqlParser.DATEADDContext ctx) {
		String dp = ctx.getChild(2).accept(this).trim();
		String increment = ctx.getChild(4).accept(this).trim();
		String date = ctx.getChild(6).accept(this).trim();
		
		return " " + date + " + (" + increment + " * INTERVAL '1 " + dp + "')"; 
	}

	@Override
	public String visitDATEDIFF(TSqlParser.DATEDIFFContext ctx) {
		String dp = ctx.getChild(2).accept(this).trim();
		String startDate = ctx.getChild(4).accept(this).trim();
		String endDate = ctx.getChild(6).accept(this).trim();
		switch (dp) {
		case "year":
			return " DATE_PART('year', " + endDate + "::date) - DATE_PART('year', " + startDate + "::date)";
		case "month":
			return " (DATE_PART('year', " + endDate + "::date) - DATE_PART('year', " + startDate
					+ "::date)) * 12 + DATE_PART('month', " + endDate + "::date) - DATE_PART('month', " + startDate
					+ "::date)";
		case "week":
			return " TRUNC(DATE_PART('day', " + endDate + "::timestamp - " + startDate + "::timestamp)/7)";
		case "day":
			return " CASE WHEN " + endDate + "::timestamp > " + startDate + "::timestamp THEN "
		           + "(EXTRACT(DAY FROM justify_interval((" + endDate + "::timestamp - " + startDate + "::timestamp) + INTERVAL '0.99999999999 days')))"
		           + " ELSE "
		           + " - (EXTRACT(DAY FROM justify_interval((" + startDate + "::timestamp - " + endDate + "::timestamp) + INTERVAL '0.99999999999 days'))) END ";
		case "hour":
			return " ((((DATE_PART('day', " + endDate + "::timestamp - " + startDate + "::timestamp) * 24 + " + 
	                "DATE_PART('hour', " + endDate + "::timestamp - " + startDate + "::timestamp)) * 60 + " +
	                "DATE_PART('minute', " + endDate + "::timestamp - " + startDate + "::timestamp)) * 60 + " +
	                "DATE_PART('second', " + endDate + "::timestamp - " + startDate + "::timestamp))/3600)::int";
		case "minute":
			return " ((((DATE_PART('day', " + endDate + "::timestamp - " + startDate + "::timestamp) * 24 + " + 
            "DATE_PART('hour', " + endDate + "::timestamp - " + startDate + "::timestamp)) * 60 + " +
            "DATE_PART('minute', " + endDate + "::timestamp - " + startDate + "::timestamp)) * 60 + " +
            "DATE_PART('second', " + endDate + "::timestamp - " + startDate + "::timestamp))/60)::int";
		case "second":
			return " ((DATE_PART('day', " + endDate + "::timestamp - " + startDate + "::timestamp) * 24 + " + 
	                "DATE_PART('hour', " + endDate + "::timestamp - " + startDate + "::timestamp)) * 60 + " +
	                "DATE_PART('minute', " + endDate + "::timestamp - " + startDate + "::timestamp)) * 60 + " +
	                "DATE_PART('second', " + endDate + "::timestamp - " + startDate + "::timestamp)";
			
		}
		throw new UnsupportedOperationException("Unsupported operation");
	}

	@Override
	public String visitDatepart(TSqlParser.DatepartContext ctx) {
		String dp = visitChildren(ctx).trim();
		switch (dp) {
		case "year":
		case "yy":
		case "yyyy":
			return "year";
		case "quarter":
		case "qq":
		case "q":
			return "quarter";
		case "month":
		case "mm":
		case "m":
			return "month";
		case "dayofyear":
		case "dy":
		case "y":
			return "dayofyear";
		case "day":
		case "dd":
		case "d":
			return "day";
		case "week":
		case "wk":
		case "ww":
			return "week";
		case "hour":
		case "hh":
			return "hour";
		case "minute":
		case "mi":
		case "n":
			return "minute";
		case "second":
		case "ss":
		case "s":
			return "second";
		case "millisecond":
		case "ms":
			return "millisecond";
		case "microsecond":
		case "mcs":
			return "microsecond";
		case "nanosecond":
		case "ns":
			return "nanosecond";
		default:
			throw new IllegalArgumentException("Unsupported datepart value found: " + dp);
		}
	}

	private String skipNChildren(RuleNode node, int k) {
		String result = defaultResult();
		int n = node.getChildCount();
		for (int i = k; i < n; i++) {
			if (!shouldVisitNextChild(node, result)) {
				break;
			}

			ParseTree c = node.getChild(i);
			String childResult = c.accept(this);
			result = aggregateResult(result, childResult);
		}
		return " " + result;
	}

}
