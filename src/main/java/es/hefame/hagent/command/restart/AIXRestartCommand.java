package es.hefame.hagent.command.restart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.processor.result.AIXProcessorResult;

public class AIXRestartCommand extends RestartCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	RESTART_CMD_MARKER	= MarkerManager.getMarker("RESTART_CMD");

	@Override
	public AIXProcessorResult operate()
	{
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(RESTART_CMD_MARKER, "hagent", "restart");
			c.run();
			return null;
		}
		catch (Exception e)
		{
			L.catching(e);
			return null;
		}
	}

}
