package es.hefame.hagent.configuration.prtg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class PrtgProcessorConfiguration implements ConfigurationModule
{
	private static Logger		L				= LogManager.getLogger();
	public static final String	BASE_NODE_NAME	= "processor";

	// Static defaults
	public static final String	ERROR_PERCENT	= null;
	public static final String	WARN_PERCENT	= null;
	public static final int		SAMPLE_TIME		= 30;

	private ConfigurationReader	config_reader;
	private JSONObject			base_node;

	// Par�metros de configuracion
	public String				error_percent	= ERROR_PERCENT;
	public String				warn_percent	= WARN_PERCENT;
	public int					sample_time		= SAMPLE_TIME;

	public PrtgProcessorConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = (JSONObject) this.config_reader.get_object(PrtgConfiguration.BASE_NODE_NAME).get(BASE_NODE_NAME);

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. No se monitorizara el procesador");
			return;
		}

		this.read_error_percent();
		this.read_warn_percent();
		this.read_sample_time();

	}

	private void read_error_percent()
	{
		String param_name = "error_percent";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				int tmp = Integer.parseInt(o.toString());
				this.error_percent = "" + tmp;
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.error_percent = ERROR_PERCENT;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.error_percent = ERROR_PERCENT;
		}

	}

	private void read_warn_percent()
	{
		String param_name = "warn_percent";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				int tmp = Integer.parseInt(o.toString());
				this.warn_percent = "" + tmp;
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.warn_percent = WARN_PERCENT;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.warn_percent = WARN_PERCENT;
		}
	}

	private void read_sample_time()
	{
		String param_name = "sample_time";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				this.sample_time = Integer.parseInt(o.toString());
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.sample_time = SAMPLE_TIME;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.sample_time = SAMPLE_TIME;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("error_percent", this.error_percent);
		root.put("warn_percent", this.warn_percent);
		root.put("sample_time", this.sample_time);
		return root;
	}

}
