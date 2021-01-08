package es.hefame.hagent.configuration.alert_channel;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hagent.util.mail.Mailer;

public class MailerAlertChannel extends AlertChannel
{
	private static Logger	L	= LogManager.getLogger();

	private List<String>	to	= new LinkedList<String>();

	public static MailerAlertChannel create_default_channel()
	{
		MailerAlertChannel channel = new MailerAlertChannel("default");
		channel.to.add("cpd.sistemas.unix@hefame.es");
		return channel;
	}

	public static MailerAlertChannel create_success_channel()
	{
		MailerAlertChannel channel = new MailerAlertChannel("success");
		// channel.to.add("david.madrid@hefame.es");
		return channel;
	}

	private MailerAlertChannel(String name)
	{
		super(name, "mailer");
	}

	public MailerAlertChannel(String name, JSONObject data) throws HException
	{
		super(name, "mailer");

		String param_name = "to";
		Object to_array = data.get(param_name);
		if (to_array != null)
		{
			if (to_array instanceof JSONArray)
			{
				for (Object mailaddr : (JSONArray) to_array)
				{
					this.to.add(mailaddr.toString());
				}
			}
			else
			{
				this.to.add(to_array.toString());
			}
		}
		else
		{
			L.error("No se encuentra el parametro obligatorio [{}]. Se ignora el canal.", param_name);
			throw new HException("No se encuentra el parametro obligatorio [" + param_name + "] en el canal de alertas.");
		}
	}

	private boolean send(String toaddr, String title, String message)
	{
		Mailer m = new Mailer();
		m.to(toaddr);
		m.from(AgentInfo.get_hostname().toUpperCase());
		m.subject(title);
		m.html(message);
		m.send();
		return true;
	}

	public boolean send(String title, String message)
	{
		for (String dest : this.to)
		{
			this.send(dest, title, message);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("to", to);
		return root;
	}

}
