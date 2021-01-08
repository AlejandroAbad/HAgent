package es.hefame.hagent.command.memory.result;

import java.util.StringTokenizer;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class LNXMemoryResult extends MemoryResult
{
	protected long	ram_cached_bytes;
	protected long	ram_shared_bytes;
	protected long	ram_buffer_bytes;

	/*
	 * ENTRADA ESPERADA
	 * ************************************************************************************
	 * total used free shared buffers cached
	 * Mem: 4150272000 3917701120 232570880 161337344 4866048 3208990720
	 * Swap: 4294963200 454656 4294508544
	 * 
	 * ************************************************************************************
	 */

	public LNXMemoryResult(OsCommandResult command_result)
	{
		if (command_result.in_error())
		{
			// this.set_in_error("Ocurrio un error al ejecutar el comando:\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\n");

		if (nbTokenizer.countTokens() != 3)
		{
			// this.set_in_error("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 3]\n" + command_result.toString() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		// Descarta primera linea que es de cabecera
		nbTokenizer.nextToken();

		// Memoria Fisica
		String line = nbTokenizer.nextToken().trim();
		String[] ram_tokens = line.split("\\s+");
		if (ram_tokens.length != 7)
		{
			// his.set_in_error("Numero de tokens incorrecto al obtener la memoria fisica [" + ram_tokens.length + " != 7] en la linea [" + line + "]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_total_bytes = Long.parseLong(ram_tokens[1]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el total de bytes de memoria fisica, no se pudo convertir [" + ram_tokens[1] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_cached_bytes = Long.parseLong(ram_tokens[6]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes cacheados en memoria fisica, no se pudo convertir [" + ram_tokens[6] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_buffer_bytes = Long.parseLong(ram_tokens[5]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes de buffers en memoria fisica, no se pudo convertir [" + ram_tokens[5] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_shared_bytes = Long.parseLong(ram_tokens[4]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes shared en memoria fisica, no se pudo convertir [" + ram_tokens[4] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.ram_free_bytes = Long.parseLong(ram_tokens[3]);
			this.ram_free_bytes += this.ram_cached_bytes;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes cacheados en memoria fisica, no se pudo convertir [" + ram_tokens[3] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
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
			this.swap_total_bytes = Long.parseLong(swap_tokens[1]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer el total de bytes de memoria SWAP, no se pudo convertir [" + swap_tokens[1] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.swap_used_bytes = Long.parseLong(swap_tokens[2]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Al leer los bytes usados de memoria SWAP, no se pudo convertir [" + swap_tokens[2] + "] a long: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.swap_free_bytes = this.swap_total_bytes - this.swap_used_bytes;
		this.swap_used_bytes_percentage = (this.swap_total_bytes > 0) ? (this.swap_used_bytes * 100) / this.swap_total_bytes : 0;

		this.channelize();
		this.addChannel(new PrtgChannelResult("Memoria fisica en cache", this.ram_cached_bytes, "BytesDisk"));
		this.addChannel(new PrtgChannelResult("Memoria fisica en buffers", this.ram_buffer_bytes, "BytesDisk"));
		this.addChannel(new PrtgChannelResult("Memoria fisica compartida", this.ram_shared_bytes, "BytesDisk"));
	}

}
