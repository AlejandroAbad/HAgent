package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;

public class BrArchiveConfigData extends ArchivelogConfigData
{
	private static Logger	L	= LogManager.getLogger();

	private String			sapFile;
	private String			brUser;
	private String			brOption;

	public BrArchiveConfigData(JSONObject jsonRoot) throws HException
	{
		super(jsonRoot, "br");
		L.debug("Parseando informacion ESPECIFICA del objeto BRARCHIVE");

		String elementName;
		Object elementObj;


		elementName = "sap_file";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			sapFile = elementObj.toString();
		}
		else
		{
			sapFile = "init" + this.dbName.toUpperCase() + ".sap";
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, sapFile);
		}

		elementName = "br_user";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null) {
			brUser = elementObj.toString();
		} else {
			brUser = "//";
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, brUser);
		}

		elementName = "br_option";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null) {
			brOption = elementObj.toString();
		} else {
			brOption = "-sd";
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, brOption);
		}

	}

	public String getSapFile()
	{
		return sapFile;
	}

	public String getBrUser()
	{
		return brUser;
	}

	public String getBrOption()
	{
		return brOption;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("sap_file", this.sapFile);
		root.put("br_user", this.brUser);
		root.put("br_option", this.brOption);
		return root;
	}

}
