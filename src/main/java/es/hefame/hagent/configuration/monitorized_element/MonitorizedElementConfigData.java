package es.hefame.hagent.configuration.monitorized_element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.common.AlertlogConfigData;
import es.hefame.hagent.configuration.monitorized_element.common.ErrptConfigData;
import es.hefame.hagent.configuration.monitorized_element.common.ProcessListConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.AsmDiskgroupsConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleAlertlogConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleGridResourcesConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.StandbyGapConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.BrArchiveConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.DeleteArchiveConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.TdpoArchiveConfigData;
import es.hefame.hagent.configuration.monitorized_element.proyman.ProymanImpresoraConfigData;
import es.hefame.hagent.configuration.monitorized_element.proyman.ProymanIsap3060ConfigData;

public abstract class MonitorizedElementConfigData implements JsonEncodable
{
	private static Logger														L			= LogManager.getLogger();

	private static Map<String, Class<? extends MonitorizedElementConfigData>>	TYPES_MAP	= new HashMap<String, Class<? extends MonitorizedElementConfigData>>();

	static
	{

		// COMMON
		TYPES_MAP.put("errpt", ErrptConfigData.class);
		TYPES_MAP.put("alertlog", AlertlogConfigData.class);
		TYPES_MAP.put("process_list", ProcessListConfigData.class);

		// PROYMAN
		TYPES_MAP.put("proyman_impresora", ProymanImpresoraConfigData.class);
		TYPES_MAP.put("proyman_isap3060", ProymanIsap3060ConfigData.class);

		// ORACLE
		TYPES_MAP.put("oracle_grid_resources", OracleGridResourcesConfigData.class);
		TYPES_MAP.put("oracle_alertlog", OracleAlertlogConfigData.class);
		TYPES_MAP.put("asm_diskgroups", AsmDiskgroupsConfigData.class);
		TYPES_MAP.put("standby_gap", StandbyGapConfigData.class);

		// ORACLE ARCHIVELOG
		TYPES_MAP.put("delete_archive", DeleteArchiveConfigData.class);
		TYPES_MAP.put("tdpo_archive", TdpoArchiveConfigData.class);
		TYPES_MAP.put("br_archive", BrArchiveConfigData.class);

	}

	protected String	type;
	protected String	name;

	public static Class<? extends MonitorizedElementConfigData> get_type_class(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es null");
		Object o_type = json_root.get("type");
		if (o_type == null) throw new HException("No se encuentra el campo 'type' en el elemento de configuracion");

		String type = o_type.toString();

		if (TYPES_MAP.containsKey(type))
		{
			return TYPES_MAP.get(type);
		}
		else
		{
			throw new HException("No se encuentra el tipo de elemento de configuracion [" + type + "]");
		}

	}

	public static MonitorizedElementConfigData create(JSONObject json_root) throws HException
	{
		L.trace("Construyendo elemento monitorizable con el nodo {}", json_root.toJSONString());

		try
		{
			Class<? extends MonitorizedElementConfigData> type_class = MonitorizedElementConfigData.get_type_class(json_root);
			Constructor<? extends MonitorizedElementConfigData> constructor = type_class.getConstructor(JSONObject.class);
			return constructor.newInstance(json_root);
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw L.throwing(new HException("Error al leer el objeto de configuracion", e));
		}

	}

	public final String get_name()
	{
		return this.name;
	}

	public final String get_type()
	{
		return this.type;
	}
}
