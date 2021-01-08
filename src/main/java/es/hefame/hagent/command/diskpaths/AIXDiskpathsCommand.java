
package es.hefame.hagent.command.diskpaths;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.diskpaths.result.AIXDiskpathsResult;
import es.hefame.hagent.util.exception.CommandException;

public class AIXDiskpathsCommand extends DiskpathsCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	DISKPATHS_CMD_MARKER	= MarkerManager.getMarker("DISKPATHS_CMD");

	@Override
	public AIXDiskpathsResult operate() throws CommandException
	{
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(DISKPATHS_CMD_MARKER, "lspath", "-F", "name:parent:status:path_id");
			OsCommandResult command_result = c.run();
			return new AIXDiskpathsResult(command_result);
		}
		catch (IOException e)
		{
			L.catching(e);
			throw new CommandException("Error al ejecutar el comando", e);
		}

	}

}
