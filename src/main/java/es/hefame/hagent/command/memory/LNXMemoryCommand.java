
package es.hefame.hagent.command.memory;

import java.io.IOException;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.memory.result.LNXMemoryResult;
import es.hefame.hagent.util.exception.CommandException;

public class LNXMemoryCommand extends MemoryCommand
{
	private static final Marker MEMORY_CMD_MARKER = MarkerManager.getMarker("MEM_CMD");

	@Override
	public LNXMemoryResult operate() throws CommandException
	{
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(MEMORY_CMD_MARKER, "free", "-b", "-o");
			OsCommandResult command_result = c.run();

			// Memoria del sistema
			return new LNXMemoryResult(command_result);

		}
		catch (IOException e)
		{
			throw new CommandException("Error al ejecutar el comando", e);
		}

	}

}
