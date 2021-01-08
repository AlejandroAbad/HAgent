package es.hefame.hagent.command.os.level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.os.level.result.WINOsLevelResult;

public class WINOsLevelCommand extends OsLevelCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	OSLEVEL_CMD_MARKER	= MarkerManager.getMarker("OSLEVEL_CMD");

	public WINOsLevelResult operate()
	{
		OsCommandExecutor c = new OsCommandExecutor(OSLEVEL_CMD_MARKER, "powershell.exe", "[environment]::OSversion.Version", "|", "Format-Table", "-HideTableHeaders");
		OsCommandResult result;
		try
		{
			result = c.run();
			WINOsLevelResult oslevel = new WINOsLevelResult(result);
			L.trace(OSLEVEL_CMD_MARKER, "Resultado obtenido: {}", oslevel.jsonEncode());
			return oslevel;
		}
		catch (Exception e)
		{
			L.catching(e);
			return new WINOsLevelResult();
		}
	}

}
