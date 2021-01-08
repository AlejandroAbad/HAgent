package es.hefame.hagent.util.mail.html;

public class HtmlAnchor
{
	private Object	text	= "";
	private String	url		= "";

	public HtmlAnchor(Object text, String url)
	{
		this.text = text;
		this.url = url;
	}

	public HtmlAnchor(String url)
	{
		this(url, url);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"").append(this.url).append("\">");
		sb.append(text);
		sb.append("</a>");
		return sb.toString();

	}

}
