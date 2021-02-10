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

	protected String			dbName;
	protected String			osUser;

	protected String			archiveDest;
	protected int				archivePercent;

	protected String			alertChannel	= ALERT_CHANNEL;

	public ArchivelogConfigData(JSONObject jsonRoot, String subtype) throws HException
	{
		if (jsonRoot == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto ARCHIVELOG");

		this.type = "archivelog";
		this.subtype = subtype;

		String elementName;
		Object elementObj;

		// DB NAME (obligatorio)
		elementName = "db_name";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			dbName = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		// USER (obligatorio)
		elementName = "user";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			osUser = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		// ARCHIVE DEST (obligatorio)
		elementName = "archive_dest";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			archiveDest = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		// ARCHIVE PERCENT (obligatorio y en el rango de 0 a 100)
		elementName = "archive_percent";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			try
			{
				archivePercent = Integer.parseInt(elementObj.toString());

				if (archivePercent > 100 || archivePercent < 0)
				{
					L.debug("El valor de '{}' es [{}]. Debe encontrarse entre 0 y 100", elementName, archivePercent);
					throw new HException("No se encuentra el parametro [" + elementName + "]");
				}
			}
			catch (NumberFormatException e)
			{
				L.catching(e);
				L.debug("El valor de '{}' no es un entero valido. Este es obligatorio para el tipo de objeto", elementName);
				throw new HException("El parametro [" + elementName + "] no es un entero valido");
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		// ALERT CHANNEL (opcional)
		elementName = "alert_channel";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			alertChannel = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, ALERT_CHANNEL);
		}

		this.name = "archivelog_" + dbName.toLowerCase();

	}

	public String getSubtype()
	{
		return subtype;
	}

	public String getDbName()
	{
		return dbName;
	}

	public String getUser()
	{
		return osUser;
	}

	public String getArchiveDest()
	{
		return archiveDest;
	}

	public int getArchivePercent()
	{
		return archivePercent;
	}

	public String getAlertChannel()
	{
		return alertChannel;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("subtype", this.subtype);
		root.put("name", this.name);
		root.put("db_name", this.dbName);
		root.put("user", this.osUser);
		root.put("alert_channel", this.alertChannel);
		root.put("archive_dest", this.archiveDest);
		root.put("archive_percent", this.archivePercent);
		return root;
	}

}
