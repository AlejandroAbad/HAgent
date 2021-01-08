package es.hefame.hagent.configuration.monitorized_element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class MonitorizedElementConfiguration implements ConfigurationModule
{
	private static Logger					L				= LogManager.getLogger();

	// Static defaults
	private ConfigurationReader				config_reader;
	private JSONArray						base_node;
	public static final String				BASE_NODE_NAME	= "monitorized_elements";

	private Map<String, MonitorizedElementConfigData>	checkers		= new HashMap<String, MonitorizedElementConfigData>();

	// Par�metros de configuracion

	public MonitorizedElementConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
		this.config_reader.add_listener(this);
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = this.config_reader.get_array(BASE_NODE_NAME);
		this.checkers = new HashMap<String, MonitorizedElementConfigData>();

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [{}]. No se carga ningun elemento monitorizado", BASE_NODE_NAME);
			return;
		}

		L.info("Configuracion de ELEMENTOS DE MONITORIZACION");

		@SuppressWarnings("unchecked")
		Iterator<Object> it = base_node.iterator();
		while (it.hasNext())
		{
			Object o = it.next();
			if (o != null && o instanceof JSONObject)
			{
				JSONObject obj = (JSONObject) o;
				try
				{
					MonitorizedElementConfigData config_element = MonitorizedElementConfigData.create(obj);
					this.checkers.put(config_element.get_name(), config_element);
					L.info("\t[{}] -> [{}]", config_element.get_name(), config_element.jsonEncode());
				}
				catch (HException e)
				{
					L.error("Excepcion mientra se instanciaba el elemento de configuracion. Se ignora el nodo.");
					L.catching(e);
				}
			}
			else
			{
				L.error("Encontrado un elemento en el array [{}] que no es un Objeto JSON. Se ignora el nodo.", o);
			}
		}
	}

	public MonitorizedElementConfigData getMonitorizedElementByName(String name)
	{
		return this.checkers.get(name);
	}

	public List<MonitorizedElementConfigData> get_element_of_type(String type)
	{
		List<MonitorizedElementConfigData> elements = new LinkedList<MonitorizedElementConfigData>();

		for (MonitorizedElementConfigData element : this.checkers.values())
		{
			if (element.get_type().equalsIgnoreCase(type))
			{
				elements.add(element);
			}
		}
		return elements;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray jsonEncode()
	{
		JSONArray root = new JSONArray();

		for (MonitorizedElementConfigData config_element : this.checkers.values())
		{
			root.add(config_element.jsonEncode());
		}

		return root;
	}
}
