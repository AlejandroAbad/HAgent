package es.hefame.hagent.configuration.monitorized_element.common;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class ProcessListConfigData extends MonitorizedElementConfigData
{
	private static Logger		L				= LogManager.getLogger();

	public static final String	ALERT_CHANNEL	= "default";

	private List<String>		process_patterns;

	public ProcessListConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto PROCESS LIST");

		this.name = "process_list";
		this.type = "process_list";
		this.process_patterns = new LinkedList<String>();

		String element_name;
		Object element_obj;

		element_name = "process_patterns";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			if (element_obj instanceof JSONArray)
			{
				JSONArray processes = (JSONArray) element_obj;
				for (Object o : processes)
				{
					if (o instanceof String)
					{
						this.process_patterns.add(o.toString());
					}
					else
					{
						L.warn("Se ignora el objeto [{}]: no es de tipo string [{}]", o, o.getClass().getName());
					}
				}
			}
			else
			{
				L.warn("El valor de '{}' no es un array JSON", element_name);
				throw new HException("No es un array json [" + element_name + "]");
			}
		}
		else
		{
			L.warn("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}
	}

	public List<String> get_patterns()
	{
		return this.process_patterns;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);
		JSONArray patterns = new JSONArray();
		for (String p : this.process_patterns)
		{
			patterns.add(p);
		}
		root.put("process_patterns", patterns);

		return root;
	}

}
