package es.hefame.hagent.util.mail.html;

public class HtmlParagraph
{
	private Object	text	= new Object[0];
	private String	style	= null;

	public HtmlParagraph(Object text, String style)
	{
		this.text = text;
		this.style = style;
	}

	public HtmlParagraph(Object text)
	{
		this(text, null);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<p");
		if (this.style != null) sb.append(" class=\"").append(style).append("\"");
		sb.append(">");
		sb.append(text);
		sb.append("</p>");
		return sb.toString();

	}

}
