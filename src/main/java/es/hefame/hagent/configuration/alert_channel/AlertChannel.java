package es.hefame.hagent.configuration.alert_channel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;

public abstract class AlertChannel implements JsonEncodable
{
	private static Logger	L	= LogManager.getLogger();

	private String			name;
	private String			type;

	public AlertChannel(String name, String type)
	{
		this.name = name;
		this.type = type;
	}

	public String get_name()
	{
		return this.name;
	}

	public String get_type()
	{
		return this.type;
	}

	public abstract boolean send(String title, String message);

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("name", name);
		root.put("type", type);
		return root;
	}

	public static AlertChannel new_instance(JSONObject data) throws HException
	{
		String type;
		String name;
		String param_name;
		Object o;

		param_name = "name";
		o = data.get(param_name);
		if (o != null)
		{
			name = o.toString();
		}
		else
		{
			L.error("No se encuentra el parametro obligatorio [{}]. Se ignora el canal.", param_name);
			throw new HException("No se encuentra el parametro obligatorio [" + param_name + "] en el canal de alertas.");
		}

		param_name = "type";
		o = data.get(param_name);
		if (o != null)
		{
			type = o.toString();
		}
		else
		{
			L.error("No se encuentra el parametro obligatorio [{}]. Se ignora el canal.", param_name);
			throw new HException("No se encuentra el parametro obligatorio [" + param_name + "] en el canal de alertas.");
		}

		switch (type)
		{
			case "mailer":
				return new MailerAlertChannel(name, data);
		}

		throw new HException("No se reconoce el tipo de canal de alerta [" + type + "]");

	}

}
