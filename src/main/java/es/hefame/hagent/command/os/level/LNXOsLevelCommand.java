package es.hefame.hagent.command.os.level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.os.level.result.LNXOsLevelResult;

public class LNXOsLevelCommand extends OsLevelCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	OSLEVEL_CMD_MARKER	= MarkerManager.getMarker("OSLEVEL_CMD");

	public LNXOsLevelResult operate()
	{
		OsCommandExecutor c = new OsCommandExecutor(OSLEVEL_CMD_MARKER, "cat", "/etc/SuSE-release").pipe("grep", "=");
		OsCommandExecutor kernel_c = new OsCommandExecutor(OSLEVEL_CMD_MARKER, "uname", "-r");
		OsCommandResult result, kernel_result;
		try
		{
			result = c.run();
			kernel_result = kernel_c.run();
			LNXOsLevelResult oslevel = new LNXOsLevelResult(result, kernel_result);
			L.trace(OSLEVEL_CMD_MARKER, "Resultado obtenido:", oslevel.jsonEncode());

			return oslevel;
		}
		catch (Exception e)
		{
			L.catching(e);
			return new LNXOsLevelResult();
		}
	}

}
