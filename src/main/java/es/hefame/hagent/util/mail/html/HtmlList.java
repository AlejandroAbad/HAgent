package es.hefame.hagent.util.mail.html;

import java.util.LinkedList;
import java.util.List;

public class HtmlList
{
	private List<Object>	items	= new LinkedList<Object>();
	private String			type	= "ul";

	public HtmlList(List<Object> items, String type)
	{
		this.items = items;
		this.type = type;
	}

	public HtmlList(String type)
	{
		this(new LinkedList<Object>(), type);
	}

	public HtmlList()
	{
		this(new LinkedList<Object>(), "ul");
	}

	public HtmlList add_item(Object o)
	{
		this.items.add(o);
		return this;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(type).append('>');

		for (Object o : this.items)
		{
			sb.append("<li>").append(o.toString()).append("</li>");
		}

		sb.append("</").append(type).append('>');
		return sb.toString();

	}

}
