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
import es.hefame.hagent.command.interfaces.result.WINInterfaceResult;
import es.hefame.hagent.util.exception.CommandException;

public class WINInterfacesCommand extends InterfacesCommand
{
	private static Logger		L				= LogManager.getLogger();
	private static final Marker	IF_CMD_MARKER	= MarkerManager.getMarker("IF_CMD");

	@Override
	public Map<String, WINInterfaceResult> operate()
	{
		Map<String, WINInterfaceResult> results = new HashMap<String, WINInterfaceResult>();
		try
		{
			String[] cmd = { "powershell.exe", // Windows mola
					"get-WmiObject", "Win32_PerfFormattedData_Tcpip_NetworkInterface", //
					"|", "Select-Object", "Name,BytesReceivedPersec,BytesSentPersec", //
					"|", "Format-Table", "-HideTableHeaders" //
			};

			OsCommandExecutor interfaces_cmd = new OsCommandExecutor(IF_CMD_MARKER, cmd);
			OsCommandResult interfaces_cmd_result = interfaces_cmd.run();

			if (interfaces_cmd_result.in_error()) { throw new CommandException(interfaces_cmd_result, "Ocurrio un error al ejecutar el comando"); }

			StringTokenizer nbTokenizer = new StringTokenizer(new String(interfaces_cmd_result.get_stdout()), "\r\n");
			String line;
			String[] tokens;

			while (nbTokenizer.hasMoreElements())
			{
				line = nbTokenizer.nextToken();
				tokens = line.split("\\s+");
				WINInterfaceResult result = new WINInterfaceResult(tokens);
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
