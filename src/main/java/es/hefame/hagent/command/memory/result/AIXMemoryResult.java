package es.hefame.hagent.command.memory.result;

import java.util.StringTokenizer;

import es.hefame.hcore.converter.DiskSizeConverter;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class AIXMemoryResult extends MemoryResult
{

	/*
	 * Unit: KB
	 * --------------------------------------------------------------------------------------
	 * size inuse free pin virtual available mmode
	 * memory 67108864 65783060 1325804 11925532 56479448 7732808 Ded
	 * pg space 33488896 155636
	 * 
	 * work pers clnt other
	 * pin 9552552 0 159156 2213824
	 * in use 56479448 0 9303612
	 */

	public AIXMemoryResult(OsCommandResult command_result)
	{
		if (command_result.in_error())
		{
			// this.set_in_error("Ocurrio un error al ejecutar el comando:\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\n");

		if (nbTokenizer.countTokens() != 8)
		{
			// this.set_in_error("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 8]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		// Descarta primeras 3 lineas que son de cabecera
		for (int i = 0; i < 3; i++)
		{
			nbTokenizer.nextToken();
		}

		// Memoria Fisica
		String line = nbTokenizer.nextToken().trim();
		String[] ram_tokens = line.split("\\s+");
		if (ram_tokens.length != 8)
		{
			// this.set_in_error("Numero de tokens incorrecto al obtener la memoria fisica [" + ram_tokens.length + " != 8] en la linea [" + line + "]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_total_bytes = Long.parseLong(ram_tokens[1]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el total de bytes de memoria fisica, no se pudo convertir [" + ram_tokens[1] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_free_bytes = Long.parseLong(ram_tokens[6]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes libres de memoria fisica, no se pudo convertir [" + ram_tokens[8] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.ram_used_bytes = this.ram_total_bytes - this.ram_free_bytes;
		this.ram_used_bytes_percentage = (this.ram_total_bytes > 0) ? (this.ram_used_bytes * 100) / this.ram_total_bytes : 0;

		// SWAP
		line = nbTokenizer.nextToken().trim();
		String[] swap_tokens = line.split("\\s+");
		if (swap_tokens.length != 4)
		{
			// this.set_in_error("Numero de tokens incorrecto al obtener la memoria SWAP [" + swap_tokens.length + " != 4] en la linea [" + line + "]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.swap_total_bytes = Long.parseLong(swap_tokens[2]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el total de bytes de memoria SWAP, no se pudo convertir [" + swap_tokens[2] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.swap_used_bytes = Long.parseLong(swap_tokens[3]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes usados de memoria SWAP, no se pudo convertir [" + swap_tokens[3] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.swap_free_bytes = this.swap_total_bytes - this.swap_used_bytes;
		this.swap_used_bytes_percentage = (this.swap_total_bytes > 0) ? (this.swap_used_bytes * 100) / this.swap_total_bytes : 0;

		this.channelize();

	}

}
