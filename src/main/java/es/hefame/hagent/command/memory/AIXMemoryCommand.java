
package es.hefame.hagent.command.memory;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.memory.result.AIXMemoryResult;
import es.hefame.hagent.util.exception.CommandException;

public class AIXMemoryCommand extends MemoryCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	MEMORY_CMD_MARKER	= MarkerManager.getMarker("MEM_CMD");

	@Override
	public AIXMemoryResult operate() throws CommandException
	{
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(MEMORY_CMD_MARKER, "svmon", "-O", "unit=KB");
			OsCommandResult command_result = c.run();

			return new AIXMemoryResult(command_result);
		}
		catch (IOException e)
		{
			throw L.throwing(new CommandException("Error al ejecutar el comando", e));
		}

	}

}
