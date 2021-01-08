package es.hefame.hagent.configuration.monitorized_element.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class ErrptConfigData extends MonitorizedElementConfigData
{
	private static Logger		L				= LogManager.getLogger();

	public static final long	CHECK_INTERVAL	= 60000;
	public static final String	TYPE_FILTER		= "INFO,PEND,PERF,PERM,TEMP,UNKN";
	public static final String	ALERT_CHANNEL	= "default";

	private long				check_interval	= CHECK_INTERVAL;
	private String				type_filter		= TYPE_FILTER;
	private String				alert_channel	= ALERT_CHANNEL;

	public ErrptConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto ERRPT");
		this.type = "errpt";
		this.name = "errpt";

		String element_name;
		Object element_obj;

		element_name = "check_interval";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			try
			{
				check_interval = Long.parseLong(element_obj.toString());
			}
			catch (NumberFormatException e)
			{
				L.warn("No se pudo convertir [{}] al leer el valor de [{}]. Se usa el valor por defecto [{}]", element_obj, element_name, CHECK_INTERVAL);
				L.catching(e);
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, CHECK_INTERVAL);
		}

		element_name = "type_filter";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			type_filter = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, TYPE_FILTER);
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

	}

	public long get_check_interval()
	{
		return check_interval;
	}

	public String get_type_filter()
	{
		return type_filter;
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
		root.put("name", this.name);
		root.put("check_interval", this.check_interval);
		root.put("type_filter", this.type_filter);
		root.put("alert_channel", this.alert_channel);
		return root;
	}

}
