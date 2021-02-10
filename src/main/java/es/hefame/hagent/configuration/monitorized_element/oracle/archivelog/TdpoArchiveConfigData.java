package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;

public class TdpoArchiveConfigData extends ArchivelogConfigData
{
	private static Logger	L	= LogManager.getLogger();

	private String			tdpoOptfile;
	private String			extraEnv;
	private String			oracleHome;

	public TdpoArchiveConfigData(JSONObject jsonRoot) throws HException
	{
		super(jsonRoot, "tdpo");
		L.debug("Parseando informacion ESPECIFICA del objeto TDPO");

		String elementName;
		Object elementObj;

		elementName = "tdpo_optfile";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			tdpoOptfile = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		elementName = "extra_env";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			extraEnv = elementObj.toString();
		}
		else
		{
			extraEnv = "";
			L.debug("No se haya el valor de '{}'. Se deja la cadena vacia", elementName);
		}
		
		elementName = "oracle_home";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			oracleHome = elementObj.toString();
		}
		else
		{
			oracleHome = "";
			L.debug("No se haya el valor de '{}'. Se deja la cadena vacia", elementName);
		}

		extraEnv = extraEnv.trim();
		tdpoOptfile = tdpoOptfile.trim();
		oracleHome = oracleHome.trim();
	}

	public String getTdpoOptfile()
	{
		return tdpoOptfile;
	}

	public String getExtraEnv()
	{
		if (extraEnv != null && extraEnv.length() > 0) return extraEnv + ", ";
		return "";
	}
	
	public String getOracleHome() {
		return oracleHome;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("tdpo_optfile", this.tdpoOptfile);
		root.put("extra_env", this.extraEnv);
		return root;
	}

}
