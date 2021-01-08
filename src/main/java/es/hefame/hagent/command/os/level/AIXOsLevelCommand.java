package es.hefame.hagent.command.os.level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.os.level.result.AIXOsLevelResult;

public class AIXOsLevelCommand extends OsLevelCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	OSLEVEL_CMD_MARKER	= MarkerManager.getMarker("OSLEVEL_CMD");

	@Override
	public AIXOsLevelResult operate()
	{
		OsCommandExecutor c = new OsCommandExecutor(OSLEVEL_CMD_MARKER, "oslevel", "-s");
		OsCommandResult result;
		try
		{
			result = c.run();
			AIXOsLevelResult oslevel = new AIXOsLevelResult(result);
			L.trace(OSLEVEL_CMD_MARKER, "Resultado obtenido:", oslevel.jsonEncode());
			return oslevel;
		}
		catch (Exception e)
		{
			L.catching(e);
			return new AIXOsLevelResult();

		}
	}

}
