package es.hefame.hagent.command.processor.result;

import java.util.StringTokenizer;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class LNXProcessorResult extends ProcessorResult
{

	protected double	user_load_percentaje;
	protected double	system_load_percentaje;
	protected double	idle_load_percentaje;
	protected double	iowait_load_percentaje;
	protected double	stale_load_percentaje;
	protected int		process_queue;

	/*
	 * FORMATO ESPERADO
	 * *******************************************************************************************
	 * procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
	 * r b swpd free buff cache si so bi bo in cs us sy id wa st
	 * 0 0 444 230604 4752 3131612 0 0 1 2 3 5 0 0 100 0 0
	 * 
	 * *******************************************************************************************
	 */

	public LNXProcessorResult(OsCommandResult command_result) throws CommandException
	{
		if (command_result.in_error())
		{
			// this.set_in_error("Ocurrio un error al ejecutar el comando:\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\n");

		if (nbTokenizer.countTokens() != 4)
		{
			// this.set_in_error("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 4]" + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Descarta primeras 3 lineas que son de cabecera
		for (int i = 0; i < 3; i++)
		{
			nbTokenizer.nextToken();
		}

		String line = nbTokenizer.nextToken().trim();
		String[] tokens = line.split("\\s+");

		if (tokens.length != 17)
		{
			// this.set_in_error("Numero de tokens incorrecto [" + tokens.length + " != 17] en la linea [" + line + "]" + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Procesos en cola
		try
		{
			this.process_queue = Integer.parseInt(tokens[0]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la carga de usuario del procesador, no se pudo convertir [" + tokens[12] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el numero de procesos en cola."));
			return;
		}

		// Carga de CPU de usuario
		try
		{
			this.user_load_percentaje = Double.parseDouble(tokens[12]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la carga de usuario del procesador, no se pudo convertir [" + tokens[12] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Carga de CPU de sistema
		try
		{
			this.system_load_percentaje = Double.parseDouble(tokens[13]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la carga de sistema del procesador, no se pudo convertir [" + tokens[13] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Carga de CPU en idle
		try
		{
			this.idle_load_percentaje = Double.parseDouble(tokens[14]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la el tiempo idle del procesador, no se pudo convertir [" + tokens[14] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Carga de CPU en IO wait
		try
		{
			this.iowait_load_percentaje = Double.parseDouble(tokens[15]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la carga de iowait del procesador, no se pudo convertir [" + tokens[15] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Carga de CPU en stale
		try
		{
			this.stale_load_percentaje = Double.parseDouble(tokens[16]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer la carga de stale del procesador, no se pudo convertir [" + tokens[16] + "] a double: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return;
		}

		// Carga total
		this.load_percentage = 100 - this.idle_load_percentaje;

		this.channelize();
		this.addChannel(new PrtgChannelResult("Procesos encolados", this.process_queue, "Count"));
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en usuario", this.user_load_percentaje, "Percent"));
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en sistema", this.system_load_percentaje, "Percent"));
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en iowait", this.iowait_load_percentaje, "Percent"));
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en stale", this.stale_load_percentaje, "Percent"));
	}

}
