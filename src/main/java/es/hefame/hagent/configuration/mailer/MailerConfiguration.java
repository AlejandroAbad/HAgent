package es.hefame.hagent.configuration.mailer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;
import es.hefame.hagent.util.agent.AgentInfo;

public class MailerConfiguration implements ConfigurationModule
{

	private static Logger		L				= LogManager.getLogger();

	// Static defaults
	public static final String	SMTP_SERVER		= "correo.hefame.es";
	public static final String	SMTP_USER		= null;
	public static final String	SMTP_PASS		= null;
	public static final int		SMTP_PORT		= 25;
	public static final boolean	SMTP_TLS		= false;
	public static final String	MAIL_FROM		= "hagent@" + AgentInfo.get_fqdn_hostname();
	public static final String	REPLY_TO		= "noresponder@" + AgentInfo.get_fqdn_hostname();

	private ConfigurationReader	config_reader;
	private JSONObject			base_node;
	public static final String	BASE_NODE_NAME	= "mailer";

	// Par�metros de configuracion

	public String				smtp_server		= SMTP_SERVER;
	public String				smtp_user		= SMTP_USER;
	public String				smtp_pass		= SMTP_PASS;
	public int					smtp_port		= SMTP_PORT;
	public boolean				smtp_tls		= SMTP_TLS;
	public String				mail_from		= MAIL_FROM;
	public String				reply_to		= REPLY_TO;

	public MailerConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
		this.config_reader.add_listener(this);
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = this.config_reader.get_object(BASE_NODE_NAME);

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se utilizara la configuracion de correo por defecto");
		}
		else
		{
			this.read_smtp_server();
			this.read_smtp_user();
			this.read_smtp_pass();
			this.read_smtp_port();
			this.read_smtp_tls();
			this.read_mail_from();
			this.read_reply_to();
		}

		L.info("Configuracion de CORREO leida [{}]", this.jsonEncode().toJSONString());
	}

	private void read_smtp_server()
	{
		String param_name = "smtp_server";
		String default_value = SMTP_SERVER;
		this.smtp_server = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = this.base_node.get(param_name);
		if (o != null)
		{
			this.smtp_server = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_smtp_user()
	{
		String param_name = "smtp_user";
		String default_value = SMTP_USER;
		this.smtp_user = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = base_node.get(param_name);
		if (o != null)
		{
			this.smtp_user = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_smtp_pass()
	{
		String param_name = "smtp_pass";
		String default_value = SMTP_PASS;
		this.smtp_pass = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = base_node.get(param_name);
		if (o != null)
		{
			this.smtp_pass = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_smtp_port()
	{
		String param_name = "smtp_port";
		int default_value = SMTP_PORT;
		this.smtp_port = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = this.base_node.get(param_name);
		if (o != null)
		{
			try
			{
				this.smtp_port = Integer.parseInt(o.toString());
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]. Mensaje de excepcion [" + e.getMessage() + "]. Se asume por defecto [ " + default_value + " ]");
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_smtp_tls()
	{
		String param_name = "smtp_tls";
		boolean default_value = SMTP_TLS;
		this.smtp_tls = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = this.base_node.get(param_name);
		if (o != null)
		{
			try
			{
				this.smtp_tls = Boolean.parseBoolean(o.toString());
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a booleano el valor de [ " + param_name + " ]. Mensaje de excepcion [" + e.getMessage() + "]. Se asume por defecto [ " + default_value + " ]");
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_mail_from()
	{
		String param_name = "mail_from";
		String default_value = MAIL_FROM;
		this.mail_from = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = base_node.get(param_name);
		if (o != null)
		{
			this.mail_from = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_reply_to()
	{
		String param_name = "reply_to";
		String default_value = REPLY_TO;
		this.reply_to = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = base_node.get(param_name);
		if (o != null)
		{
			this.reply_to = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("smtp_server", this.smtp_server);
		root.put("smtp_user", this.smtp_user);
		root.put("smtp_pass", "******");// this.smtp_pass);
		root.put("smtp_port", this.smtp_port);
		root.put("smtp_tls", this.smtp_tls);
		root.put("mail_from", this.mail_from);
		root.put("reply_to", this.reply_to);
		return root;
	}
}
