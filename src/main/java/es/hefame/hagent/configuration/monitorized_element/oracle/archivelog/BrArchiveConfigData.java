package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;

public class BrArchiveConfigData extends ArchivelogConfigData
{
	private static Logger	L	= LogManager.getLogger();

	private String			sap_file;
	private String			br_user;
	private String			br_option;

	public BrArchiveConfigData(JSONObject json_root) throws HException
	{
		super(json_root, "br");
		L.debug("Parseando informacion ESPECIFICA del objeto BRARCHIVE");

		String element_name;
		Object element_obj;


		element_name = "sap_file";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			sap_file = element_obj.toString();
		}
		else
		{
			sap_file = "init" + this.db_name.toUpperCase() + ".sap";
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, sap_file);
		}

		element_name = "br_user";
		element_obj = json_root.get(element_name);
		if (element_obj != null) {
			br_user = element_obj.toString();
		} else {
			br_user = "//";
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, br_user);
		}

		element_name = "br_option";
		element_obj = json_root.get(element_name);
		if (element_obj != null) {
			br_option = element_obj.toString();
		} else {
			br_option = "-sd";
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, br_option);
		}

	}

	public String get_sap_file()
	{
		return sap_file;
	}

	public String get_br_user()
	{
		return br_user;
	}

	public String get_br_option()
	{
		return br_option;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("sap_file", this.sap_file);
		root.put("br_user", this.br_user);
		root.put("br_option", this.br_option);
		return root;
	}

}
