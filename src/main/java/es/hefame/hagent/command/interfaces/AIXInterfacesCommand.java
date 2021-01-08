package es.hefame.hagent.command.interfaces;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.interfaces.result.AIXInterfaceResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.util.exception.CommandException;

public class AIXInterfacesCommand extends InterfacesCommand
{
	private static Logger		L				= LogManager.getLogger();
	private static final Marker	IF_CMD_MARKER	= MarkerManager.getMarker("IF_CMD");

	@Override
	public Map<String, AIXInterfaceResult> operate()
	{
		Map<String, AIXInterfaceResult> results = new HashMap<String, AIXInterfaceResult>();

		try
		{
			OsCommandExecutor c = new OsCommandExecutor(IF_CMD_MARKER, "ifconfig", "-l");
			OsCommandResult iface_list = c.run();

			if (iface_list.in_error()) { throw new CommandException(iface_list, "Ocurrio un error al ejecutar el comando"); }

			StringTokenizer nbTokenizer = new StringTokenizer(new String(iface_list.get_stdout()), "\n");

			if (nbTokenizer.countTokens() < 1) { throw new CommandException(iface_list, "el numero de lineas devueltas es incorrecto [" + nbTokenizer.countTokens() + " < 1]"); }

			String[] lo_interface_names = nbTokenizer.nextToken().trim().split("\\s+");

			for (int i = 0; i < lo_interface_names.length; i++)
			{
				// Ignoramos interfaces de loopback
				if (lo_interface_names[i].startsWith("lo") || !CONF.prtg.interfaces.is_configured(lo_interface_names[i]))
				{
					continue;
				}

				OsCommandExecutor get_iface_cmd = new OsCommandExecutor(IF_CMD_MARKER, "entstat", "-t", lo_interface_names[i]);
				OsCommandResult get_iface_cmd_result = get_iface_cmd.run();

				AIXInterfaceResult result = new AIXInterfaceResult(get_iface_cmd_result);
				results.put(result.get_interface_name(), result);

				// Reseteamos las estadisticas interfaz
				new OsCommandExecutor(IF_CMD_MARKER, "entstat", "-r", lo_interface_names[i]).run();
			}

		}
		catch (Exception e)
		{
			L.catching(e);
		}

		return results;
	}

}
