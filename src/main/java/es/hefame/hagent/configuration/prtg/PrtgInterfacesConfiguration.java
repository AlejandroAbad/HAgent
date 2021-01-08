package es.hefame.hagent.configuration.prtg;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class PrtgInterfacesConfiguration implements ConfigurationModule
{
	private static Logger						L						= LogManager.getLogger();
	public static final String					BASE_NODE_NAME			= "interfaces";

	private ConfigurationReader					config_reader;
	private JSONArray							base_node;

	// Par�metros de configuracion
	private Map<String, InterfaceConfigData>	lo_interfaces_config	= new LinkedHashMap<String, InterfaceConfigData>();

	public PrtgInterfacesConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = (JSONArray) this.config_reader.get_object(PrtgConfiguration.BASE_NODE_NAME).get(BASE_NODE_NAME);

		this.lo_interfaces_config = new LinkedHashMap<String, InterfaceConfigData>();

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. No se monitorizar� ningun interfaz");
			return;
		}

		Iterator<?> it = this.base_node.iterator();
		int counter = 0;
		while (it.hasNext())
		{
			counter++;
			try
			{
				JSONObject iface_json_data = (JSONObject) it.next();
				InterfaceConfigData ifcd = new InterfaceConfigData(iface_json_data);
				this.lo_interfaces_config.put(ifcd.get_interface_name(), ifcd);
			}
			catch (Exception e)
			{
				L.error("Excepcion parseando el interfaz numero [" + counter + "]");
				L.catching(e);
			}

		}

	}

	public boolean is_available()
	{
		return (base_node != null && lo_interfaces_config.size() > 0);
	}

	public Set<String> list_names()
	{
		return this.lo_interfaces_config.keySet();
	}

	public Iterator<InterfaceConfigData> iterator()
	{
		return this.lo_interfaces_config.values().iterator();
	}

	public InterfaceConfigData get(String key)
	{
		return this.lo_interfaces_config.get(key);
	}

	public boolean is_configured(String key)
	{
		return (this.lo_interfaces_config.get(key) != null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray jsonEncode()
	{
		JSONArray cn = new JSONArray();
		for (InterfaceConfigData s : lo_interfaces_config.values())
		{
			cn.add(s.jsonEncode());
		}
		return cn;
	}

	public class InterfaceConfigData implements JsonEncodable
	{

		private String	interface_name;
		private boolean	check_online	= true;

		public InterfaceConfigData(JSONObject json) throws HException
		{
			if (json == null) { throw new HException("El objeto es nulo"); }

			Object o;

			o = json.get("interface_name");
			if (o == null) { throw new HException("El atributo 'interface_name' no existe y es obligatorio."); }
			this.interface_name = o.toString().trim();

			o = json.get("check_online");
			if (o != null)
			{
				this.check_online = Boolean.parseBoolean(o.toString());
			}

		}

		public String get_interface_name()
		{
			return this.interface_name;
		}

		public boolean check_online()
		{
			return this.check_online;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject jsonEncode()
		{
			JSONObject root = new JSONObject();
			root.put("interface_name", this.interface_name);
			root.put("check_online", this.check_online);
			return root;
		}
	}

}
