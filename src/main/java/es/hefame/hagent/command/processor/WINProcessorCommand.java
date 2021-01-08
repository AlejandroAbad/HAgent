package es.hefame.hagent.command.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.processor.result.WINProcessorResult;

public class WINProcessorCommand extends ProcessorCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESSOR_CMD_MARKER	= MarkerManager.getMarker("CPU_CMD");

	@Override
	public WINProcessorResult operate()
	{
		try
		{
			// TODO: Podemos utilzar C.prtg.processor.sample_time y tomar muestras durante un intervalo de tiempo

			String[] cmd = { "powershell.exe", // Windows mola
					"get-WmiObject", "win32_Processor", // Info_del_SO
					"|", "Select-Object", "LoadPercentage", // Objetos_de_memoria
					"|", "Format-Table", "-HideTableHeaders" // Modo_tabla_sin_cabecera
			};

			OsCommandExecutor c = new OsCommandExecutor(PROCESSOR_CMD_MARKER, cmd);
			OsCommandResult command_result = c.run();

			return new WINProcessorResult(command_result);

		}
		catch (Exception e)
		{
			L.catching(e);
			return null;
		}

	}

}
