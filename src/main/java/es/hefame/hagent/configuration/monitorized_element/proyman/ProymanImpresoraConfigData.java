package es.hefame.hagent.configuration.monitorized_element.proyman;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class ProymanImpresoraConfigData extends MonitorizedElementConfigData
{
	private static Logger	L	= LogManager.getLogger();

	private String			file;

	public ProymanImpresoraConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto PROYMAN");

		this.type = "proyman_impresora";
		this.name = "proyman_impresora";

		String element_name;
		Object element_obj;

		element_name = "file";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			file = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

	}

	public String get_file()
	{
		return file;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);
		root.put("file", this.file);
		return root;
	}

}
