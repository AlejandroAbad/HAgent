
package es.hefame.hagent.command.memory;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.memory.result.WINMemoryResult;
import es.hefame.hagent.util.exception.CommandException;

public class WINMemoryCommand extends MemoryCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	MEMORY_CMD_MARKER	= MarkerManager.getMarker("MEM_CMD");

	@Override
	public WINMemoryResult operate() throws CommandException
	{
		try
		{
			String[] cmd = { "powershell.exe", // Windows mola
					"get-WmiObject", "win32_OperatingSystem", // Info_del_SO
					"|", "Select-Object", "TotalVisibleMemorySize,FreePhysicalMemory,TotalVirtualMemorySize,FreeVirtualMemory", // Objetos_de_memoria
					"|", "Format-Table", "-HideTableHeaders" // Modo_tabla_sin_cabecera
			};

			OsCommandExecutor c = new OsCommandExecutor(MEMORY_CMD_MARKER, cmd);
			OsCommandResult command_result = c.run();

			return new WINMemoryResult(command_result);
		}
		catch (IOException e)
		{
			throw L.throwing(new CommandException("Error al ejecutar el comando", e));
		}
	}

}
