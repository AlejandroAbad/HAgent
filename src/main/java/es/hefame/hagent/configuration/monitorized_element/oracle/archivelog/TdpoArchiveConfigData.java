package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;

public class TdpoArchiveConfigData extends ArchivelogConfigData
{
	private static Logger	L	= LogManager.getLogger();

	private String			tdpo_optfile;
	private String			extra_env;
	private String			oracle_home;

	public TdpoArchiveConfigData(JSONObject json_root) throws HException
	{
		super(json_root, "tdpo");
		L.debug("Parseando informacion ESPECIFICA del objeto TDPO");

		String element_name;
		Object element_obj;

		element_name = "tdpo_optfile";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			tdpo_optfile = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", element_name);
			throw new HException("No se encuentra el parametro [" + element_name + "]");
		}

		element_name = "extra_env";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			extra_env = element_obj.toString();
		}
		else
		{
			extra_env = "";
			L.debug("No se haya el valor de '{}'. Se deja la cadena vacia", element_name);
		}
		
		element_name = "oracle_home";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			oracle_home = element_obj.toString();
		}
		else
		{
			oracle_home = "";
			L.debug("No se haya el valor de '{}'. Se deja la cadena vacia", element_name);
		}

		extra_env = extra_env.trim();
		tdpo_optfile = tdpo_optfile.trim();
		oracle_home = oracle_home.trim();
	}

	public String get_tdpo_optfile()
	{
		return tdpo_optfile;
	}

	public String get_extra_env()
	{
		if (extra_env != null && extra_env.length() > 0) return extra_env + ", ";
		return "";
	}
	
	public String get_oracle_home() {
		return oracle_home;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("tdpo_optfile", this.tdpo_optfile);
		root.put("extra_env", this.extra_env);
		return root;
	}

}
