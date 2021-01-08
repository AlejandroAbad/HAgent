package es.hefame.hagent.configuration.prtg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class PrtgConfiguration implements ConfigurationModule
{
	private static Logger				L				= LogManager.getLogger();
	public static final String			BASE_NODE_NAME	= "prtg";

	private ConfigurationReader			config_reader;
	public PrtgProcessorConfiguration	processor;
	public PrtgMemoryConfiguration		memory;
	public PrtgFilesystemsConfiguration	filesystems;
	public PrtgInterfacesConfiguration	interfaces;

	public PrtgConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
		this.processor = new PrtgProcessorConfiguration(this.config_reader);
		this.memory = new PrtgMemoryConfiguration(this.config_reader);
		this.filesystems = new PrtgFilesystemsConfiguration(this.config_reader);
		this.interfaces = new PrtgInterfacesConfiguration(this.config_reader);
		this.config_reader.add_listener(this);
	}

	@Override
	public void configuration_changed()
	{
		processor.configuration_changed();
		memory.configuration_changed();
		filesystems.configuration_changed();
		interfaces.configuration_changed();

		L.info("Configuracion de PRTG leida [{}]", this.jsonEncode().toJSONString());

	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("processor", this.processor.jsonEncode());
		root.put("memory", this.memory.jsonEncode());
		root.put("filesystems", this.filesystems.jsonEncode());
		root.put("interfaces", this.interfaces.jsonEncode());
		return root;
	}

}
