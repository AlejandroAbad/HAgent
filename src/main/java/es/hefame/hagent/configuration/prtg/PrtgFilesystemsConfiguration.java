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
import es.hefame.hcore.converter.DiskSizeConverter;
import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.core.ConfigurationModule;
import es.hefame.hagent.configuration.core.ConfigurationReader;

public class PrtgFilesystemsConfiguration implements ConfigurationModule
{
	private static Logger						L						= LogManager.getLogger();

	public static final String					BASE_NODE_NAME			= "filesystems";

	private ConfigurationReader					config_reader;
	private JSONArray							base_node;

	// Par�metros de configuracion
	private Map<String, FilesystemConfigData>	lo_filesystem_config	= new LinkedHashMap<String, FilesystemConfigData>();

	public PrtgFilesystemsConfiguration(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
	}

	@Override
	public void configuration_changed()
	{
		this.base_node = (JSONArray) this.config_reader.get_object(PrtgConfiguration.BASE_NODE_NAME).get(BASE_NODE_NAME);

		if (this.base_node == null)
		{
			L.warn("No se encuentra el nodo json [ " + BASE_NODE_NAME + " ]. No se monitorizar� ningun filesystem");
			return;
		}

		this.lo_filesystem_config = new LinkedHashMap<String, FilesystemConfigData>();

		Iterator<?> it = this.base_node.iterator();
		int counter = 0;
		while (it.hasNext())
		{
			counter++;
			try
			{
				JSONObject fs_json_data = (JSONObject) it.next();
				FilesystemConfigData fscd = new FilesystemConfigData(fs_json_data);
				this.lo_filesystem_config.put(fscd.get_mount_point(), fscd);

			}
			catch (Exception e)
			{
				L.error("Excepcion parseando el filesystem numero [" + counter + "]");
				L.catching(e);
			}

		}

	}

	public boolean is_available()
	{
		return (base_node != null && lo_filesystem_config.size() > 0);
	}

	public Set<String> list_names()
	{
		return this.lo_filesystem_config.keySet();
	}

	public Iterator<FilesystemConfigData> iterator()
	{
		return this.lo_filesystem_config.values().iterator();
	}

	public FilesystemConfigData get(String key)
	{
		return this.lo_filesystem_config.get(key);
	}

	public boolean is_configured(String key)
	{
		return (this.lo_filesystem_config.get(key) != null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray jsonEncode()
	{
		JSONArray cn = new JSONArray();
		for (FilesystemConfigData s : lo_filesystem_config.values())
		{
			cn.add(s.jsonEncode());
		}
		return cn;
	}

	public class FilesystemConfigData implements JsonEncodable
	{

		private String	mount_point;
		private boolean	check_mounted				= true;
		private boolean	check_inodes				= false;
		private String	warn_percent				= null;
		private String	error_percent				= null;
		private String	warn_free					= null;
		private String	error_free					= null;
		private String	inodes_warn_free_percent	= null;
		private String	inodes_error_free_percent	= null;
		private boolean	cluster_fs					= false;

		public FilesystemConfigData(JSONObject json) throws HException
		{
			if (json == null) { throw new HException("El objeto es nulo"); }
			Object o;

			// Filesystem name (obligatorio)
			o = json.get("filesystem");
			if (o == null) { throw new HException("El atributo 'filesystem' no existe y es obligatorio."); }
			this.mount_point = o.toString();

			// Check Mounted (opcional, defecto true)
			o = json.get("check_mounted");
			if (o != null && o instanceof Boolean)
			{
				this.check_mounted = ((Boolean) o).booleanValue();
			}

			// Check Inodes (opcional, defecto false)
			o = json.get("check_inodes");
			if (o != null && o instanceof Boolean)
			{
				this.check_inodes = ((Boolean) o).booleanValue();
			}

			// warn_percent (opcional, defecto -1)
			o = json.get("warn_percent");
			if (o != null)
			{
				warn_percent = o.toString();
			}

			// error_percent (opcional, defecto -1)
			o = json.get("error_percent");
			if (o != null)
			{
				error_percent = o.toString();
			}

			// warn_free (opcional, defecto -1)
			o = json.get("warn_free");
			if (o != null)
			{
				warn_free = String.valueOf(DiskSizeConverter.parseDiskSize(o.toString()));
			}

			// error_free (opcional, defecto -1)
			o = json.get("error_free");
			if (o != null)
			{
				error_free = String.valueOf(DiskSizeConverter.parseDiskSize(o.toString()));
			}

			// inodes_warn_percent (opcional, defecto -1)
			o = json.get("inodes_warn_percent");
			if (o != null)
			{
				inodes_warn_free_percent = o.toString();
			}

			// inodes_error_percent (opcional, defecto -1)
			o = json.get("inodes_error_percent");
			if (o != null)
			{
				inodes_error_free_percent = o.toString();
			}

			// cluster (opcional, defecto false)
			o = json.get("cluster");
			if (o != null && o instanceof Boolean)
			{
				this.cluster_fs = ((Boolean) o).booleanValue();
			}

		}

		public String get_mount_point()
		{
			return mount_point;
		}

		public boolean check_mounted()
		{
			return check_mounted;
		}

		public boolean is_check_inodes()
		{
			return check_inodes;
		}

		public String get_warn_percent()
		{
			return warn_percent;
		}

		public String get_error_percent()
		{
			return error_percent;
		}

		public String get_warn_free()
		{
			return warn_free;
		}

		public String get_error_free()
		{
			return error_free;
		}

		public String get_inodes_warn_free_percent()
		{
			return inodes_warn_free_percent;
		}

		public String get_inodes_error_free_percent()
		{
			return inodes_error_free_percent;
		}

		public boolean is_cluster_fs()
		{
			return cluster_fs;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONObject jsonEncode()
		{
			JSONObject root = new JSONObject();
			root.put("mount_point", this.mount_point);
			if (this.check_inodes == false) root.put("check_mounted", this.check_mounted);
			if (this.check_inodes == true) root.put("check_inodes", this.check_inodes);
			if (this.warn_percent != null) root.put("warn_percent", this.warn_percent);
			if (this.error_percent != null) root.put("error_percent", this.error_percent);
			if (this.warn_free != null) root.put("warn_free", this.warn_free);
			if (this.error_free != null) root.put("error_free", this.error_free);
			if (this.inodes_warn_free_percent != null) root.put("inodes_warn_free_percent", this.inodes_warn_free_percent);
			if (this.inodes_error_free_percent != null) root.put("inodes_error_free_percent", this.inodes_error_free_percent);
			if (this.cluster_fs == true) root.put("cluster", this.cluster_fs);
			return root;
		}

	}

}
