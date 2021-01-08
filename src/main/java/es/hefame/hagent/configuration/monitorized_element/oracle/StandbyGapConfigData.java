package es.hefame.hagent.configuration.monitorized_element.oracle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class StandbyGapConfigData extends MonitorizedElementConfigData
{
	private static Logger	L					= LogManager.getLogger();

	private final String	STB_USER			= "HAGENT";
	private final String	STB_PASSWORD		= "passw0rd";
	private final String	STB_TNS				= "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=STB)))";

	private final String	PRIMARY_USER		= "HAGENT";
	private final String	PRIMARY_PASSWORD	= "passw0rd";
	private final String	PRIMARY_TNS			= "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=PRIMARY)))";

	private String			stbUser				= STB_USER;
	private String			stbPassword			= STB_PASSWORD;
	private String			stbTns				= STB_TNS;

	private String			primaryUser			= PRIMARY_USER;
	private String			primaryPassword		= PRIMARY_PASSWORD;
	private String			primaryTns			= PRIMARY_TNS;

	public StandbyGapConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto STANDBY GAP");

		this.type = "standby_gap";
		this.name = "standby_gap";

		String elementName;
		Object elementObj;

		elementName = "stb_user";
		elementObj = json_root.get(elementName);
		if (elementObj != null)
		{
			stbUser = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, STB_USER);
		}

		elementName = "stb_password";
		elementObj = json_root.get(elementName);
		if (elementObj != null)
		{
			stbPassword = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, STB_PASSWORD);
		}

		elementName = "stb_tns";
		elementObj = json_root.get(elementName);
		if (elementObj != null)
		{
			stbTns = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, STB_TNS);
		}

		elementName = "primary_user";
		elementObj = json_root.get(elementName);
		if (elementObj != null)
		{
			primaryUser = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, PRIMARY_USER);
		}

		elementName = "primary_password";
		elementObj = json_root.get(elementName);
		if (elementObj != null)
		{
			primaryPassword = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, PRIMARY_PASSWORD);
		}

		elementName = "primary_tns";
		elementObj = json_root.get(elementName);
		if (elementObj != null)
		{
			primaryTns = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", elementName, PRIMARY_TNS);
		}

	}

	public String getStbUser()
	{
		return stbUser;
	}

	public String getStbPassword()
	{
		return stbPassword;
	}

	public String getStbTns()
	{
		return stbTns;
	}

	public String getPrimaryUser()
	{
		return primaryUser;
	}

	public String getPrimaryPassword()
	{
		return primaryPassword;
	}

	public String getPrimaryTns()
	{
		return primaryTns;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);
		root.put("stb_user", this.stbUser);
		root.put("stb_password", "******");// this.stbPassword);
		root.put("stb_tns", this.stbTns);
		root.put("primary_user", this.primaryUser);
		root.put("primary_password", "******");// this.primaryPassword);
		root.put("primary_tns", this.primaryTns);
		return root;
	}

}
