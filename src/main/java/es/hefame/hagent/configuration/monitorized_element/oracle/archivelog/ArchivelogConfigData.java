package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class ArchivelogConfigData extends MonitorizedElementConfigData
{
	private static Logger		L				= LogManager.getLogger();

	public static final String	ALERT_CHANNEL	= "default";
	protected String			subtype;

	protected String			db_name;
	protected String			user;

	protected String			archive_dest;
	protected int				archive_percent;

	protected String			alert_channel	= ALERT_CHANNEL;

	public ArchivelogConfigData(JSONObject json_root, String subtype) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto ARCHIVELOG");

		this.type = "archivelog";
		this.subtype = subtype;

		String element_name;
		Object element_obj;

		// DB NAME (obligatorio)
		element_name = "db_name";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			db_name = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

		// USER (obligatorio)
		element_name = "user";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			user = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

		// ARCHIVE DEST (obligatorio)
		element_name = "archive_dest";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			archive_dest = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

		// ARCHIVE PERCENT (obligatorio y en el rango de 0 a 100)
		element_name = "archive_percent";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			try
			{
				archive_percent = Integer.parseInt(element_obj.toString());

				if (archive_percent > 100 || archive_percent < 0)
				{
					L.debug("El valor de '{}' es [{}]. Debe encontrarse entre 0 y 100", element_name, archive_percent);
					throw new HException("No se encuentra el parametro [" + element_name + "]");
				}
			}
			catch (NumberFormatException e)
			{
				L.catching(e);
				L.debug("El valor de '{}' no es un entero valido. Este es obligatorio para el tipo de objeto", element_name);
				throw new HException("El parametro [" + element_name + "] no es un entero valido");
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

		// ALERT CHANNEL (opcional)
		element_name = "alert_channel";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			alert_channel = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, ALERT_CHANNEL);
		}

		this.name = "archivelog_" + db_name.toLowerCase();

	}

	public String get_subtype()
	{
		return subtype;
	}

	public String get_db_name()
	{
		return db_name;
	}

	public String get_user()
	{
		return user;
	}

	public String get_archive_dest()
	{
		return archive_dest;
	}

	public int get_archive_percent()
	{
		return archive_percent;
	}

	public String get_alert_channel()
	{
		return alert_channel;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("subtype", this.subtype);
		root.put("name", this.name);
		root.put("db_name", this.db_name);
		root.put("user", this.user);
		root.put("alert_channel", this.alert_channel);
		root.put("archive_dest", this.archive_dest);
		root.put("archive_percent", this.archive_percent);
		return root;
	}

}
