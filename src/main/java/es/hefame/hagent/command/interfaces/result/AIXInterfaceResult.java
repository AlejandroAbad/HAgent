package es.hefame.hagent.command.interfaces.result;

import java.util.StringTokenizer;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class AIXInterfaceResult extends InterfaceResult
{
	/*
	 * FORMATO ESPERADO
	 * *******************************************************************************************
	 * --------------------------------------------------------
	 * ETHERNET STATISTICS (en3) :
	 * Device Type: Virtual I/O Ethernet Adapter (l-lan)
	 * Hardware Address: aa:be:d7:2e:d8:f9
	 * Elapsed Time: 0 days 0 hours 0 minutes 35 seconds
	 * 
	 * Transmit Statistics: Receive Statistics:
	 * -------------------- -------------------
	 * Packets: 25 Packets: 30
	 * Bytes: 2494 Bytes: 3080
	 * Interrupts: 0 Interrupts: 30
	 * Transmit Errors: 0 Receive Errors: 0
	 * Packets Dropped: 0 Packets Dropped: 0
	 * Bad Packets: 0
	 * Max Packets on S/W Transmit Queue: 0
	 * S/W Transmit Queue Overflow: 0
	 * Current S/W+H/W Transmit Queue Length: 0
	 * 
	 * Broadcast Packets: 0 Broadcast Packets: 2
	 * Multicast Packets: 0 Multicast Packets: 22
	 * No Carrier Sense: 0 CRC Errors: 0
	 * DMA Underrun: 0 DMA Overrun: 0
	 * Lost CTS Errors: 0 Alignment Errors: 0
	 * Max Collision Errors: 0 No Resource Errors: 0
	 * Late Collision Errors: 0 Receive Collision Errors: 0
	 * Deferred: 0 Packet Too Short Errors: 0
	 * SQE Test: 0 Packet Too Long Errors: 0
	 * Timeout Errors: 0 Packets Discarded by Adapter: 0
	 * Single Collision Count: 0 Receiver Start Count: 0
	 * Multiple Collision Count: 0
	 * Current HW Transmit Queue Length: 0
	 * 
	 * General Statistics:
	 * -------------------
	 * No mbuf Errors: 0
	 * Adapter Reset Count: 0
	 * Adapter Data Rate: 20000
	 * Driver Flags: Up Broadcast Running
	 * Simplex 64BitSupport ChecksumOffload
	 * DataRateSet VIOENT
	 * 
	 * *******************************************************************************************
	 */

	public AIXInterfaceResult(OsCommandResult command_result)
	{
		if (command_result.in_error())
		{
			// this.set_in_error("Ocurrio un error al ejecutar el comando:\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\n");

		if (nbTokenizer.countTokens() < 34)
		{
			// this.set_in_error("El numero de lineas es incorrecto [" + nbTokenizer.countTokens() + " < 34]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		String line;
		String[] tokens;

		// Interfaces
		nbTokenizer.nextToken();
		line = nbTokenizer.nextToken();
		tokens = line.split("\\s+");

		if (tokens.length != 4)
		{
			// this.set_in_error("El numero de tokens en [" + line + "]es incorrecto al leer el nombre del interfaz [" + tokens.length + " != 4]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		if (tokens[2].length() < 3)
		{
			// this.set_in_error("El nombre del interfaz [" + tokens[2] + "] es demasiado corto [" + tokens[2].length() + " < 3]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.interface_name = tokens[2].substring(1, tokens[2].length() - 1);

		// Tiempo
		for (int i = 0; i < 3; i++)
		{
			line = nbTokenizer.nextToken();
		}

		tokens = line.split("\\s+");

		if (tokens.length != 10)
		{
			// this.set_in_error("El numero de tokens en [" + line + "] es incorrecto al leer el tiempo del interfaz [" + tokens.length + " != 10]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		long sample_secs = 0;
		try
		{
			sample_secs = Long.parseLong(tokens[8]);
			sample_secs += Long.parseLong(tokens[6]) * 60;
			sample_secs += Long.parseLong(tokens[4]) * 60 * 60;
			sample_secs += Long.parseLong(tokens[2]) * 60 * 60 * 24;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Excepcion [" + e.getClass().getName() + "] al convertir el tiempo a valor numerico.\n\tMensaje: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		// Bytes
		for (int i = 0; i < 4; i++)
		{
			line = nbTokenizer.nextToken();
		}

		tokens = line.split("\\s+");

		if (tokens.length != 4)
		{
			// this.set_in_error("El numero de tokens en [" + line + "] es incorrecto al leer el numero de bytes del interfaz [" + tokens.length + " != 4]\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		long tx_bytes = 0;
		long rx_bytes = 0;
		try
		{
			tx_bytes = Long.parseLong(tokens[1]);
			rx_bytes = Long.parseLong(tokens[3]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error("Excepcion [" + e.getClass().getName() + "] al convertir los bytes a valor numerico.\n\tMensaje: " + e.getMessage() + "\n" + command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		if (sample_secs > 0)
		{
			this.rx_bytes_per_second = rx_bytes / sample_secs;
			this.tx_bytes_per_second = tx_bytes / sample_secs;
		}
		else
		{
			this.rx_bytes_per_second = rx_bytes;
			this.tx_bytes_per_second = tx_bytes;
		}
		this.total_bytes_per_second = this.rx_bytes_per_second + this.tx_bytes_per_second;

		this.channelize();

	}

}
