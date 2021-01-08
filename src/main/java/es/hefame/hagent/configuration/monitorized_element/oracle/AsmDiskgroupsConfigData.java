package es.hefame.hagent.configuration.monitorized_element.oracle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class AsmDiskgroupsConfigData extends MonitorizedElementConfigData
{
	private static Logger	L				= LogManager.getLogger();

	private final String	ASM_USER		= "HAGENT";
	private final String	ASM_PASSWORD	= "passw0rd";
	private final String	ASM_TNS			= "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=+ASM)))";

	private String			asm_user		= ASM_USER;
	private String			asm_password	= ASM_PASSWORD;
	private String			asm_tns			= ASM_TNS;

	public AsmDiskgroupsConfigData(JSONObject json_root) throws HException
	{
		if (json_root == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto ASM DISKGROUPS");

		this.type = "asm_diskgroups";
		this.name = "asm_diskgroups";

		String element_name;
		Object element_obj;

		element_name = "asm_user";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			asm_user = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, ASM_USER);
		}

		element_name = "asm_password";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			asm_password = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, ASM_PASSWORD);
		}

		element_name = "asm_tns";
		element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			asm_tns = element_obj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Se usa el valor por defecto [{}]", element_name, ASM_TNS);
		}

	}

	public String get_user()
	{
		return asm_user;
	}

	public String get_password()
	{
		return asm_password;
	}

	public String get_tns()
	{
		return asm_tns;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);
		root.put("asm_user", this.asm_user);
		root.put("asm_password", "******");// this.asm_password);
		root.put("asm_tns", this.asm_tns);
		return root;
	}

}
