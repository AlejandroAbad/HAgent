package es.hefame.hagent.util.mail.html;

public class HtmlStyler
{

	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<style type='text/css'>");

		sb.append("body {");
		sb.append("		font-family: verdana;");
		sb.append("}");

		sb.append("table {");
		sb.append("		border-top: 1px solid #000000;");
		sb.append("		border-left: 1px solid #000000;");
		sb.append("		border-spacing: 10px;border-collapse: separate;");
		sb.append("		width: 90%;");
		sb.append("}");

		sb.append("table td {");
		sb.append("		border-bottom: 1px solid #000000;");
		sb.append("		border-right: 1px solid #000000;");
		sb.append("}");

		sb.append("table th {");
		sb.append("		border-bottom: 1px solid #000000;");
		sb.append("		border-right: 1px solid #000000;");
		sb.append("}");

		sb.append(".error {");
		sb.append("		background-color: #ff8888;");
		sb.append("		padding: 2px 10px 2px 10px;");
		sb.append("}");

		sb.append(".warn {");
		sb.append("		background-color: #ffff88;");
		sb.append("		padding: 2px 10px 2px 10px;");
		sb.append("}");

		sb.append(".ok {");
		sb.append("		background-color: #B9EEB7;");
		sb.append("		padding: 2px 10px 2px 10px;");
		sb.append("}");

		sb.append(".code {");
		sb.append("		border: 1px solid #000088;");
		sb.append("		background-color: #ccccff;");
		sb.append("		font-family: courier;");
		sb.append("		font-size: 90%;");
		sb.append("		padding: 10px;");
		sb.append("}");

		sb.append("</style>");

		return sb.toString();

	}

}
