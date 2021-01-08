package es.hefame.hagent.configuration.alert_channel;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class AlertChannelConfiguration implements ConfigurationModule
{
	private static Logger							L	= LogManager.getLogger();
	private static final Map<String, AlertChannel>	DEFAULT_CHANNELS;

	static
	{
		DEFAULT_CHANNELS = new HashMap<String, AlertChannel>();
		DEFAULT_CHANNELS.put("default", MailerAlertChannel.create_default_channel());
		DEFAULT_CHANNELS.put("success", MailerAlertChannel.create_success_channel());
	}

	private Map<String, AlertChannel>	channels		= DEFAULT_CHANNELS;

	private ConfigurationReader			config_reader;
	private JSONArray					base_node;
	public static final String			BASE_NODE_NAME	= "alert_channels";

	public AlertChannelConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
		this.config_reader.add_listener(this);
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = this.config_reader.get_array(BASE_NODE_NAME);

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. Se utilizara la configuracion de canales por defecto");
		}
		else
		{
			for (Object o : this.base_node)
			{
				if (o instanceof JSONObject)
				{
					try
					{
						AlertChannel channel_config = AlertChannel.new_instance((JSONObject) o);
						this.channels.put(channel_config.get_name(), channel_config);
					}
					catch (HException e)
					{
						L.error("Error al leer la configuracion del canal de alertas [{}]");
						L.catching(e);
					}

				}
				else
				{
					L.error("Se ignora el canal [{}] pues no es un objeto json", o.toString());
				}
			}

		}
		L.info("Configuracion de CANALES DE ALERTA leida [{}]", this.jsonEncode().toJSONString());
	}

	public AlertChannel get_channel(String name)
	{
		AlertChannel channel = this.channels.get(name);
		if (channel != null) return channel;
		return this.channels.get("default");
	}

	public AlertChannel get_channel_if_exists(String name)
	{
		return this.channels.get(name);
	}

	@SuppressWarnings("unchecked")
	public JSONArray jsonEncode()
	{
		JSONArray achannels = new JSONArray();
		for (AlertChannel ch : channels.values())
		{
			achannels.add(ch.jsonEncode());
		}
		return achannels;

	}

}
