package es.hefame.hagent.command.processor.result;

import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class WINProcessorResult extends ProcessorResult
{
	private static Logger L = LogManager.getLogger();

	/*
	 * FORMATO ESPERADO
	 * *******************************************************************************************
	 * 
	 * 6
	 * 
	 * 
	 * *******************************************************************************************
	 */

	public WINProcessorResult(OsCommandResult command_result)
	{
		L.info("TEST TEST");
		if (command_result.in_error())
		{
			// this.set_in_error("Ocurrio un error al ejecutar el comando:\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\r\n");

		if (nbTokenizer.countTokens() != 1)
		{
			// this.set_in_error("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 1]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		String line = nbTokenizer.nextToken().trim();
		String[] tokens = line.split("\\s+");

		if (tokens.length != 1)
		{
			// this.set_in_error("Numero de tokens incorrecto [" + tokens.length + " != 19] en la linea [" + line + "]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Carga de CPU
		try
		{
			this.load_percentage = Double.parseDouble(tokens[0]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la carga del procesador, no se pudo convertir [" + tokens[0] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		this.channelize();

	}

}
