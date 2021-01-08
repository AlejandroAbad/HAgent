package es.hefame.hagent.util.mail.html;

import java.util.LinkedList;
import java.util.List;

public class HtmlTable
{
	private Object[]		header	= new Object[0];
	private List<Object[]>	items	= new LinkedList<Object[]>();

	public HtmlTable(List<Object[]> items)
	{
		this.items = items;
	}

	public HtmlTable()
	{
		this(new LinkedList<Object[]>());
	}

	public HtmlTable add_header(Object... strings)
	{
		header = strings;
		return this;
	}

	public HtmlTable add_row(Object... strings)
	{
		items.add(strings);
		return this;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<table>");

		if (this.header.length > 0)
		{
			sb.append("<tr>");
			for (Object col : this.header)
			{
				sb.append("<th>");
				sb.append(col.toString());
				sb.append("</th>");
			}
			sb.append("</tr>");
		}

		for (Object[] row : this.items)
		{
			sb.append("<tr>");
			for (Object col : row)
			{
				sb.append("<td>");
				sb.append(col.toString());
				sb.append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");

		return sb.toString();

	}

}
