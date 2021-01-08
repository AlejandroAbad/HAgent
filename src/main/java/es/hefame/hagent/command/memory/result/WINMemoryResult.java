package es.hefame.hagent.command.memory.result;

import java.util.StringTokenizer;

import es.hefame.hcore.converter.DiskSizeConverter;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class WINMemoryResult extends MemoryResult
{
	/*
	 * FORMATO ESPERADO
	 * *******************************************************************************************
	 * 
	 * 8334736 3588232 9645456 1567940
	 * 
	 * 
	 * *******************************************************************************************
	 */
	public WINMemoryResult(OsCommandResult command_result)
	{
		if (command_result.in_error())
		{
			// this.set_in_error("Ocurrio un error al ejecutar el comando:\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\r\n");

		if (nbTokenizer.countTokens() != 1)
		{
			// this.set_in_error("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 1]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		String line = nbTokenizer.nextToken().trim();
		String[] tokens = line.split("\\s+");

		if (tokens.length != 4)
		{
			// this.set_in_error("Numero de tokens incorrecto [" + tokens.length + " != 4] en la linea [" + line + "]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		// Memoria Fisica
		try
		{
			this.ram_total_bytes = Long.parseLong(tokens[0]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el total de bytes de memoria fisica, no se pudo convertir [" + tokens[0] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_free_bytes = Long.parseLong(tokens[1]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el numero de bytes libres en memoria fisica, no se pudo convertir [" + tokens[1] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.ram_used_bytes = this.ram_total_bytes - this.ram_free_bytes;
		this.ram_used_bytes_percentage = (this.ram_total_bytes > 0) ? (this.ram_used_bytes * 100) / this.ram_total_bytes : 0;

		// SWAP
		try
		{
			this.swap_total_bytes = Long.parseLong(tokens[2]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el total de bytes en memoria SWAP, no se pudo convertir [" + tokens[2] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.swap_free_bytes = Long.parseLong(tokens[3]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el numero de bytes libres en memoria SWAP, no se pudo convertir [" + tokens[3] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.swap_used_bytes = this.swap_total_bytes - this.swap_free_bytes;
		this.swap_used_bytes_percentage = (this.swap_total_bytes > 0) ? (this.swap_used_bytes * 100) / this.swap_total_bytes : 0;

		this.channelize();
	}

}
