package es.hefame.hagent.configuration.monitorized_element.oracle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.command.oracle.clusterwareresources.result.ClusterwareResourceResult;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;
import es.hefame.hagent.util.exception.ParseException;

public class OracleGridResourcesConfigData extends MonitorizedElementConfigData
{
	private static Logger			L	= LogManager.getLogger();

	public static final Set<String>	VITAL_RESOURCE_TYPES;
	static
	{
		VITAL_RESOURCE_TYPES = new TreeSet<String>();
		VITAL_RESOURCE_TYPES.add("ora.listener.type");
		VITAL_RESOURCE_TYPES.add("ora.diskgroup.type");
		VITAL_RESOURCE_TYPES.add("ora.database.type");
		VITAL_RESOURCE_TYPES.add("ora.acfs.type");
		VITAL_RESOURCE_TYPES.add("ora.registry.acfs.type");
		VITAL_RESOURCE_TYPES.add("ora.asm.type");
		VITAL_RESOURCE_TYPES.add("ora.network.type");
		VITAL_RESOURCE_TYPES.add("ora.scan_listener.type");
		VITAL_RESOURCE_TYPES.add("ora.scan_vip.type");
		VITAL_RESOURCE_TYPES.add("sap.abapenq.type");
		VITAL_RESOURCE_TYPES.add("sap.abaprep.type");
	}

	private Set<String>									mandatory_types	= VITAL_RESOURCE_TYPES;
	private Map<String, OracleGridResourceCondition>	conditions		= new HashMap<String, OracleGridResourceCondition>();

	@SuppressWarnings("unchecked")
	public OracleGridResourcesConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto ORACLE GRID RESOURCES");

		this.type = "oracle_grid_resources";
		this.name = "oracle_grid_resources";

		// MANDATORY TYPES
		String element_name = "mandatory_types";
		Object mandatory_types_node = json_root.get(element_name);
		if (mandatory_types_node != null)
		{
			if (mandatory_types_node instanceof JSONArray)
			{
				JSONArray mandatory_types = (JSONArray) mandatory_types_node;
				this.mandatory_types = new TreeSet<String>();

				Iterator<Object> it = mandatory_types.iterator();
				while (it.hasNext())
				{
					Object mandatory_type_node = it.next();
					if (mandatory_type_node instanceof String)
					{
						this.mandatory_types.add((String) mandatory_type_node);
					}
					else
					{
						L.warn("Se descarta el elemento [{}] de [{}] por no ser un String", mandatory_type_node, element_name);
					}
				}
			}
			else
			{
				L.warn("El elemento [{}] no es un array. Se usa el valor por defecto para el mismo.", element_name);
			}
		}
		else
		{
			L.debug("El elemento [{}] no existe. Se usa el valor por defecto para el mismo.", element_name);
		}

		// CONDICIONES
		element_name = "conditions";
		Object conditions_node = json_root.get(element_name);

		if (conditions_node != null)
		{
			if (conditions_node instanceof JSONArray)
			{
				JSONArray conditions_array = (JSONArray) conditions_node;
				Iterator<Object> it = conditions_array.iterator();
				while (it.hasNext())
				{

					Object condition_node = it.next();
					try
					{
						OracleGridResourceCondition condition = new OracleGridResourceCondition(condition_node);
						this.conditions.put(condition.resource, condition);
					}
					catch (ParseException e)
					{
						L.error("Se ignora la condicion del recursos por una excepcion");
						L.catching(e);
					}

				}

			}
			else
			{
				L.warn("El elemento [{}] no es un array. Se usa el valor por defecto para el mismo.", element_name);
			}
		}
		else
		{
			L.debug("El elemento [{}] no existe. Se usa el valor por defecto para el mismo.", element_name);
		}

	}

	public boolean is_type_mandatory(String type)
	{
		return this.mandatory_types.contains(type);
	}

	public OracleGridResourceCondition get_resource_condition(String resource)
	{
		return this.conditions.get(resource);
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);

		JSONArray mandatory_types_array = new JSONArray();
		for (String s : this.mandatory_types)
		{
			mandatory_types_array.add(s);
		}
		root.put("mandatory_types", mandatory_types_array);

		JSONArray conditions_array = new JSONArray();
		for (OracleGridResourceCondition s : this.conditions.values())
		{
			conditions_array.add(s.jsonEncode());
		}
		root.put("conditions", conditions_array);

		return root;
	}

	public static class OracleGridResourceCondition implements JsonEncodable
	{

		private String	resource;
		private String	location	= null;
		private int		on_mismatch	= ClusterwareResourceResult.STATUS_ERROR;

		public OracleGridResourceCondition(Object condition_node) throws ParseException
		{
			L.debug("Parseando condicion {}", condition_node);

			JSONObject condition_object = null;

			if (condition_node != null)
			{
				if (condition_node instanceof JSONObject)
				{
					condition_object = (JSONObject) condition_node;
				}
				else
				{
					throw new ParseException("El objecto de condicion no es un objeto JSON valido");
				}
			}
			else
			{
				throw new ParseException("El objecto de condicion es nulo");
			}

			String node_name;
			Object o;

			// RESOURCE NAME (obligatorio)
			node_name = "resource";
			o = condition_object.get(node_name);
			if (o != null)
			{
				this.resource = o.toString();
			}
			else
			{
				throw new ParseException("El campo [" + node_name + "] es obligatorio");
			}

			// LOCATION (opcional, por defecto null)
			node_name = "location";
			o = condition_object.get(node_name);
			if (o != null)
			{
				this.location = o.toString();
			}

			// ON_MISMATCH (opcional, por defecto ON_MISMATCH_ERROR)
			node_name = "on_mismatch";
			o = condition_object.get(node_name);
			if (o != null)
			{
				switch (o.toString().toLowerCase())
				{
					case "ignore":
						this.on_mismatch = ClusterwareResourceResult.STATUS_OK;
						break;
					case "unk":
					case "unknown":
						this.on_mismatch = ClusterwareResourceResult.STATUS_UNKNOWN;
						break;
					case "warn":
					case "warning":
						this.on_mismatch = ClusterwareResourceResult.STATUS_WARN;
						break;
					default:
						this.on_mismatch = ClusterwareResourceResult.STATUS_ERROR;
						break;
				}
			}
		}

		public String get_resource()
		{
			return this.resource;
		}

		public String get_location()
		{
			return this.location;
		}

		public int get_on_mismatch()
		{
			return this.on_mismatch;
		}

		@SuppressWarnings("unchecked")
		public JSONObject jsonEncode()
		{
			JSONObject root = new JSONObject();
			root.put("resource", this.resource);
			root.put("location", this.location);
			root.put("on_mismatch", this.on_mismatch);
			return root;
		}

	}

}
