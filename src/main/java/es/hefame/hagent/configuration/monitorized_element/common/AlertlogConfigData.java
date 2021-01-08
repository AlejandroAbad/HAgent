package es.hefame.hagent.configuration.monitorized_element.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class AlertlogConfigData extends MonitorizedElementConfigData
{
	private static Logger		L				= LogManager.getLogger();

	public static final String	ALERT_CHANNEL	= "default";
	public static final String	INCLUDE_REGEX	= "ERROR";
	public static final String	EXCLUDE_REGEX	= null;

	private String				log_name;
	private String				alert_log;
	private String				alert_channel	= ALERT_CHANNEL;

	private String				include_regex	= INCLUDE_REGEX;
	private String				exclude_regex	= EXCLUDE_REGEX;

	public AlertlogConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto ORACLE ALERTLOG");

		this.type = "alertlog";

		String element_name;
		Object element_obj;

		element_name = "name";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			log_name = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

		element_name = "alert_log";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			alert_log = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

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

		element_name = "include_regex";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			include_regex = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, INCLUDE_REGEX);
		}

		element_name = "exclude_regex";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			exclude_regex = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, EXCLUDE_REGEX);
		}

		this.name = "alertlog_" + log_name.toLowerCase();

	}

	public String get_log_name()
	{
		return log_name;
	}

	public String get_alert_log()
	{
		return alert_log;
	}

	public String get_alert_channel()
	{
		return alert_channel;
	}

	public String get_inc_regex()
	{
		return include_regex;
	}

	public String get_exc_regex()
	{
		return exclude_regex;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);
		root.put("log_name", this.log_name);
		root.put("alert_log", this.alert_log);
		root.put("alert_channel", this.alert_channel);
		root.put("include_regex", this.include_regex);
		root.put("exclude_regex", this.exclude_regex);
		return root;
	}

}
