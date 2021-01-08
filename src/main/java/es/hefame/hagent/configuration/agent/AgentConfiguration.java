package es.hefame.hagent.configuration.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class AgentConfiguration implements ConfigurationModule
{

	private static Logger		L						= LogManager.getLogger();

	// Static defaults
	public static final int		PORT					= 55555;
	public static final String	DOMAIN					= "hefame.es";
	public static final String	REGISTRATION_URL		= "https://bitacora.hefame.es/agent/register";
	public static final int		CONFIGURATION_RELOAD	= 3600;

	private ConfigurationReader	config_reader;
	private JSONObject			base_node;
	public static final String	BASE_NODE_NAME			= "agent";

	// Par�metros de configuracion
	public int					port					= PORT;
	public String				domain					= DOMAIN;
	public String				registration_url		= REGISTRATION_URL;
	public int					configuration_reload	= CONFIGURATION_RELOAD;

	public AgentConfiguration(ConfigurationReader config_reader)
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
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se utilizara la configuracion del agente por defecto");
		}
		else
		{
			this.read_port();
			this.read_domain();
			this.read_registration_url();
			this.read_configuration_reload();
		}
		L.info("Configuracion del AGENTE leida [{}]", this.jsonEncode().toJSONString());
	}

	private void read_port()
	{
		String param_name = "port";
		int default_value = PORT;
		this.port = default_value;

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
				this.port = Integer.parseInt(o.toString());
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

		// try
		// {
		// // TODO: HttpRouter.restart_if_port_changed();
		// }
		// catch (IOException e)
		// {
		// L.err("Excepcion [" + e.getClass().getName() + "] al reiniciar el servidor tras el cambio de puerto: " + e.getMessage());
		// }

	}

	private void read_domain()
	{
		String param_name = "domain";
		String default_value = DOMAIN;
		this.domain = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = base_node.get(param_name);
		if (o != null)
		{
			this.domain = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_registration_url()
	{
		String param_name = "registration_url";
		String default_value = REGISTRATION_URL;
		this.registration_url = default_value;

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se asumen valores por defecto [ " + default_value + " ] para [ " + param_name + " ]");
			return;
		}

		Object o = base_node.get(param_name);
		if (o != null)
		{
			this.registration_url = o.toString();
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ], se asume el valor por defecto [ " + default_value + " ]");
		}
	}

	private void read_configuration_reload()
	{
		String param_name = "configuration_reload";
		int default_value = CONFIGURATION_RELOAD;
		this.configuration_reload = default_value;

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
				this.configuration_reload = Integer.parseInt(o.toString());
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

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("port", this.port);
		root.put("domain", this.domain);
		root.put("registration_url", this.registration_url);
		root.put("configuration_reload", this.configuration_reload);
		return root;
	}
}
