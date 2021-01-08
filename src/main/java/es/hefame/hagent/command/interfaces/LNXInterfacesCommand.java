package es.hefame.hagent.command.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.interfaces.result.LNXInterfaceResult;
import es.hefame.hagent.util.exception.CommandException;

public class LNXInterfacesCommand extends InterfacesCommand
{
	private static Logger		L				= LogManager.getLogger();
	private static final Marker	IF_CMD_MARKER	= MarkerManager.getMarker("IF_CMD");

	@Override
	public Map<String, LNXInterfaceResult> operate()
	{
		Map<String, LNXInterfaceResult> results = new HashMap<String, LNXInterfaceResult>();
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(IF_CMD_MARKER, "ls", "/sys/class/net/");
			OsCommandResult iface_list = c.run();

			if (iface_list.in_error()) { throw new CommandException(iface_list, "Ocurrio un error al ejecutar el comando"); }

			String[] lo_interface_names = new String(iface_list.get_stdout()).trim().split("\\s+");

			for (int i = 0; i < lo_interface_names.length; i++)
			{
				L.debug(IF_CMD_MARKER, "Obteniendo datos para el interfaz [{}]", lo_interface_names[i]);

				// Ignoramos interfaces de loopback
				if (lo_interface_names[i].startsWith("lo"))
				{
					L.debug(IF_CMD_MARKER, "Ignoramos el interfaz de loopback");
					continue;
				}

				String path = "/sys/class/net/" + lo_interface_names[i] + "/statistics/*";

				OsCommandExecutor get_iface_cmd = new OsCommandExecutor(IF_CMD_MARKER, "sh", "-c", "grep -i '' " + path).pipe("cut", "-c", path.length() + "-");
				L.trace(IF_CMD_MARKER, "Ejecutando comando [{}]", get_iface_cmd);
				OsCommandResult get_iface_cmd_result = get_iface_cmd.run();
				L.trace(IF_CMD_MARKER, "Resultado del comando [{}]", get_iface_cmd_result);

				LNXInterfaceResult result = new LNXInterfaceResult(lo_interface_names[i], get_iface_cmd_result);

				L.debug(IF_CMD_MARKER, "Datos obtenidos para el interfaz [{}]", result.jsonEncode());

				results.put(result.get_interface_name(), result);
			}

		}
		catch (Exception e)
		{
			L.catching(e);
		}

		return results;
	}

}
