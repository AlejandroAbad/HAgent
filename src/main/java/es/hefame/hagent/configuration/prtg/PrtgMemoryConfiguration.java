package es.hefame.hagent.configuration.prtg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class PrtgMemoryConfiguration implements ConfigurationModule
{
	private static Logger		L						= LogManager.getLogger();
	public static final String	BASE_NODE_NAME			= "memory";

	// Static defaults
	public static final String	PHYSICAL_ERROR_PERCENT	= null;
	public static final String	PHYSICAL_WARN_PERCENT	= null;
	public static final String	SWAP_ERROR_PERCENT		= null;
	public static final String	SWAP_WARN_PERCENT		= null;

	private ConfigurationReader	config_reader;
	private JSONObject			base_node;

	// Par�metros de configuracion
	public String				physical_error_percent	= PHYSICAL_ERROR_PERCENT;
	public String				physical_warn_percent	= PHYSICAL_WARN_PERCENT;
	public String				swap_error_percent		= SWAP_ERROR_PERCENT;
	public String				swap_warn_percent		= SWAP_WARN_PERCENT;

	public PrtgMemoryConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = (JSONObject) this.config_reader.get_object(PrtgConfiguration.BASE_NODE_NAME).get(BASE_NODE_NAME);

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. No se monitorizara la memoria");
			return;
		}

		this.read_physical_error_percent();
		this.read_physical_warn_percent();
		this.read_swap_error_percent();
		this.read_swap_warn_percent();
	}

	private void read_physical_error_percent()
	{
		String param_name = "physical_error_percent";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				int tmp = Integer.parseInt(o.toString());
				this.physical_error_percent = "" + tmp;
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.physical_error_percent = PHYSICAL_ERROR_PERCENT;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.physical_error_percent = PHYSICAL_ERROR_PERCENT;
		}

	}

	private void read_physical_warn_percent()
	{
		String param_name = "physical_warn_percent";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				int tmp = Integer.parseInt(o.toString());
				this.physical_warn_percent = "" + tmp;
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.physical_warn_percent = PHYSICAL_WARN_PERCENT;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.physical_warn_percent = PHYSICAL_WARN_PERCENT;
		}
	}

	private void read_swap_error_percent()
	{
		String param_name = "swap_error_percent";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				int tmp = Integer.parseInt(o.toString());
				this.swap_error_percent = "" + tmp;
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.swap_error_percent = SWAP_ERROR_PERCENT;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.swap_error_percent = SWAP_ERROR_PERCENT;
		}

	}

	private void read_swap_warn_percent()
	{
		String param_name = "swap_warn_percent";

		Object o = base_node.get(param_name);
		if (o != null)
		{
			try
			{
				int tmp = Integer.parseInt(o.toString());
				this.swap_warn_percent = "" + tmp;
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el valor de [ " + param_name + " ]  leido: " + e.getMessage());
				this.swap_warn_percent = SWAP_WARN_PERCENT;
			}
		}
		else
		{
			L.warn("No se encuentra el parametro [ " + param_name + " ]");
			this.swap_warn_percent = SWAP_WARN_PERCENT;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("physical_error_percent", this.physical_error_percent);
		root.put("physical_warn_percent", this.physical_warn_percent);
		root.put("swap_error_percent", this.swap_error_percent);
		root.put("swap_warn_percent", this.swap_warn_percent);
		return root;
	}

}
