package es.hefame.hagent.configuration.core;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import es.hefame.hcore.HException;

public class ConfigurationReader
{
	public static final String			CONFIG_FILE_SYSPROP_NAME	= "hagent.configurationFile";
	private static Logger				L							= LogManager.getLogger();
	private File						config_file;
	private JSONObject					json_root					= null;
	private ConfigurationReloadThread	reload_thread				= null;
	private long						last_config_reload_time		= 0;
	private List<ConfigurationModule>	l_o_listeners				= new LinkedList<ConfigurationModule>();

	public ConfigurationReader() throws HException
	{
		String config_file_path = "config.json";

		L.debug("Comprobando si nos pasan el fichero de configuracion con la propiedad [{}] = [{}]", CONFIG_FILE_SYSPROP_NAME, System.getProperty(CONFIG_FILE_SYSPROP_NAME));
		if (System.getProperty(CONFIG_FILE_SYSPROP_NAME) != null)
		{
			config_file_path = System.getProperty(CONFIG_FILE_SYSPROP_NAME);
		}

		this.config_file = new File(config_file_path);

		if (!this.config_file.isFile()) { throw new HException("El fichero de configuracion no es valido [ " + config_file_path + " ] "); }
		if (!this.config_file.canWrite()) { throw new HException("El fichero de configuracion no es editable [ " + config_file_path + " ] "); }
	}

	public String get_configuration_file()
	{
		return this.config_file.getAbsolutePath();
	}

	public void add_listener(ConfigurationModule listener)
	{
		this.l_o_listeners.add(listener);
	}

	public boolean reload()
	{
		L.info("Recargando el fichero de configuracion [" + this.get_configuration_file() + "]");

		boolean config_file_has_changed = false;

		if (this.config_file.lastModified() != last_config_reload_time)
		{
			last_config_reload_time = this.config_file.lastModified();
			config_file_has_changed = true;
		}

		if (config_file_has_changed)
		{
			L.debug("Se han detectado cambios en el fichero");
			this.json_root = new JSONObject();
			try
			{
				Reader fileReader = new FileReader(this.config_file);
				this.json_root = (JSONObject) JSONValue.parseWithException(fileReader);
			}
			catch (Exception e)
			{
				L.fatal("Error al leer el fichero de configuracion. Se usaran valores por defecto");
				L.catching(e);
			}

			/*
			 * Notify all configuration listeners about the new configuration
			 * changes
			 */
			for (ConfigurationModule listener : this.l_o_listeners)
			{
				listener.configuration_changed();
			}

			/*
			 * Al cambiar la configuraci�n, es posible que el Thread de recarga
			 * de la configuracion estuvira apagado. Si no existia lo creamos, y
			 * en cualquier caso, forzamos que se reinicie el tiempo de espera.
			 * Si la configuracion establece que no debe autorecargarse la
			 * configuracion, el Thread terminar� inmediatamente.
			 */
			if (this.reload_thread == null || !this.reload_thread.isAlive())
			{
				this.reload_thread = new ConfigurationReloadThread(this);
				this.reload_thread.avoid_next_reload();
				this.reload_thread.start();
			}
			else
			{
				this.reload_thread.avoid_next_reload();
				this.reload_thread.interrupt();
			}

		}
		else
		{
			L.info("No se han detectado cambios en el fichero de configuracion");
		}

		return config_file_has_changed;
	}

	public JSONArray get_array(String key)
	{
		JSONArray o = (JSONArray) this.json_root.get(key);
		return o;
	}

	public JSONObject get_object(String key)
	{
		JSONObject o = (JSONObject) this.json_root.get(key);
		return o;
	}

	public void stop_configuration_reloader()
	{
		this.reload_thread.end();
		this.reload_thread.interrupt();
	}
}
