package es.hefame.hagent.util.mail.html;

public class HtmlHeader
{
	private Object	text	= new Object[0];
	private int		level	= 1;
	private String	style	= null;

	public HtmlHeader(Object text, int level, String style)
	{
		this.text = text;
		this.level = level;
		this.style = style;
	}

	public HtmlHeader(Object text, int level)
	{
		this(text, level, null);
	}

	public HtmlHeader(Object text)
	{
		this(text, 1, null);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<h").append(level);
		if (this.style != null) sb.append(" class=\"").append(style).append("\"");
		sb.append(">");
		sb.append(text);
		sb.append("</h").append(level).append(">");
		return sb.toString();

	}

}
