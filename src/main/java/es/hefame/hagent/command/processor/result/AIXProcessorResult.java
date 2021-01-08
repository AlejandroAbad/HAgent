package es.hefame.hagent.command.processor.result;

import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class AIXProcessorResult extends ProcessorResult
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESSOR_CMD_MARKER	= MarkerManager.getMarker("CPU_CMD");

	protected double			userLoadPercentaje;
	protected double			systemLoadPercentaje;
	protected double			iowaitLoadPercentaje;

	protected double			coresUsed;
	protected double			percentageCoresUsed;

	protected int				virtualCcores;
	protected double			coresEntitled;
	
	protected double			threadsR;	// vmstat - r : Average number of runnable kernel threads over the sampling interval. Runnable threads consist of the threads that are ready but still waiting to run, and the threads that are already running.
	protected double			threadsB;	// vmstat - b : Average number of kernel threads that are placed in the Virtual Memory Manager (VMM) wait queue (awaiting resource, awaiting input/output) over the sampling interval.
	protected double			threadsP;	// vmstat - p : Average number of threads waiting for I/O messages from raw devices. Raw devices are the devices that are directly attached to the system.
	protected double			threadsW;	// vmstat - w : Number of threads per second of time that are waiting for the file system direct I/O event to occur.


	/*
	 * FORMATO ESPERADO: VMSTAT
	 *
	 * System configuration: lcpu=56 mem=163840MB ent=5.00
	 *
	 *   kthr       memory              page              faults              cpu
	 * ----------- ----------- ------------------------ ------------ -----------------------
	 * r  b  p  w   avm   fre  fi  fo  pi  po  fr   sr  in   sy  cs us sy id wa    pc    ec
 	 * 6  0  0 12 32012645 4303865   0   0   0   0   0    0 12352 121605 28806 26 12 61  1  3.28  65.7
	 */
	private boolean processVmStatResult(OsCommandResult vmStatResult)
	{
		
		if (vmStatResult.in_error())
		{
			L.error(PROCESSOR_CMD_MARKER, "El comando vmstat retorno un error [{}]", vmStatResult.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}
		
		StringTokenizer vmstatTokenizer = new StringTokenizer(new String(vmStatResult.get_stdout()), "\n");
		
		if (vmstatTokenizer.countTokens() < 5)
		{
			L.error(PROCESSOR_CMD_MARKER, "Numero de lineas incorrecto [{} < 5] en la salida del comando vmstat [{}]", vmstatTokenizer.countTokens(), vmStatResult.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}
		
		
		// Obtenemos la linea de resultados que es la �ltima, las 4 primeras no nos interesan
		vmstatTokenizer.nextToken();
		vmstatTokenizer.nextToken();
		vmstatTokenizer.nextToken();
		vmstatTokenizer.nextToken();
		String line = vmstatTokenizer.nextToken().trim();
		
		String[] tokens = line.split("\\s+");

		if (tokens.length < 4)
		{
			L.error(PROCESSOR_CMD_MARKER, "Numero de tokens incorrecto [{} < 4] al leer los valores de uso los threads {}", tokens.length, tokens);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}
		
		
		try
		{
			threadsR = Double.parseDouble(tokens[0]);
			threadsB = Double.parseDouble(tokens[1]);
			threadsP = Double.parseDouble(tokens[2]);
			threadsW = Double.parseDouble(tokens[3]);
		}
		catch (NumberFormatException e)
		{
			L.error(PROCESSOR_CMD_MARKER, "Al leer los valores de Threads, no se pudo convertir el valor a numerico.");
			L.catching(e);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}
		

		
		return true;
	}

	
	/*
	 * FORMATO ESPERADO: LPARSTAT
	 * 
	 * System configuration: type=Shared mode=Uncapped smt=8 lcpu=56 mem=163840MB psize=24 ent=5.00
	 * 
	 * %user %sys %wait %idle physc %entc lbusy vcsw phint
	 * ----- ----- ------ ------ ----- ----- ------ ----- -----
	 * 15.7 8.7 0.3 75.3 2.36 47.3 4.0 19128 0
	 */
	private boolean processLparStatResult(OsCommandResult lparStatResult) 
	{
		if (lparStatResult.in_error())
		{
			L.error(PROCESSOR_CMD_MARKER, "El comando lparstat retorno un error [{}]", lparStatResult.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}

		StringTokenizer lparstatTokenizer = new StringTokenizer(new String(lparStatResult.get_stdout()), "\n");
		
		if (lparstatTokenizer.countTokens() < 4)
		{
			L.error(PROCESSOR_CMD_MARKER, "Numero de lineas incorrecto [{} < 4] en la salida del comando [{}]", lparstatTokenizer.countTokens(), lparStatResult.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}

		// Obtenemos la linea "System configuration"
		// nbTokenizer.nextToken();
		String line = lparstatTokenizer.nextToken().trim();
		String[] tokens = line.split("\\s+");
		if (tokens.length < 7)
		{
			L.error(PROCESSOR_CMD_MARKER, "Numero de tokens incorrecto [{} < 7] al obtener la System configuration - {}", tokens.length, tokens);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}

		if (tokens.length > 7) // Tiene procesador Shared, sacamos datos de los Virtual Cores
		{
			try
			{
				// El smt indica el n�mero de hilos por VP
				/*
				 * smt
				 * Indicates whether simultaneous multithreading is enabled or disabled in the partition. If there are two SMT threads, the row is displayed as "on." However, if there are more than two
				 * SMT threads, the number of SMT threads is displayed.
				 */
				String smtString = tokens[4].split("\\=")[1];
				int smt = 1;
				
				if (smtString.equalsIgnoreCase("on")) {
					smt = 2;
				} else {
					try {
						smt = Integer.parseInt(tokens[4].split("\\=")[1]);
					} catch (NumberFormatException ignore) {
						
					}
				}
				virtualCcores = Integer.parseInt(tokens[5].split("\\=")[1]);
				coresEntitled = Double.parseDouble(tokens[8].split("\\=")[1]);
				virtualCcores = virtualCcores / smt;
			}
			catch (NumberFormatException e)
			{
				L.error(PROCESSOR_CMD_MARKER, "Al leer la carga del procesador Shared, no se pudo convertir el valor de uso de CPU a numerico.");
				L.catching(e);
				this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
				return false;
			}
		}
		else
		{ // El procesador es Dedicated, no hay Virtual Cores.
			try
			{
				coresEntitled = Double.parseDouble(tokens[5].split("\\=")[1]);
				virtualCcores = -1;
			}
			catch (NumberFormatException e)
			{
				L.error(PROCESSOR_CMD_MARKER, "Al leer la carga del procesador Dedicated, no se pudo convertir el valor de uso de CPU a numerico.");
				L.catching(e);
				this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
				return false;
			}
		}

		// nbTokenizer.nextToken();
		lparstatTokenizer.nextToken();
		lparstatTokenizer.nextToken();

		// Carga de CPU
		line = lparstatTokenizer.nextToken().trim();
		tokens = line.split("\\s+");

		if (tokens.length < 4)
		{
			L.error(PROCESSOR_CMD_MARKER, "Numero de tokens incorrecto [{} < 4] al leer los valores de uso de CPU {}", tokens.length, tokens);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}

		try
		{
			this.userLoadPercentaje = Double.parseDouble(tokens[0]);
			this.systemLoadPercentaje = Double.parseDouble(tokens[1]);
			this.iowaitLoadPercentaje = Double.parseDouble(tokens[2]);
			this.load_percentage = userLoadPercentaje + systemLoadPercentaje + iowaitLoadPercentaje;

			if (tokens.length > 5)
			{ // Caso LPAR con procesador Shared
				this.coresUsed = Double.parseDouble(tokens[4]);
				this.percentageCoresUsed = Double.parseDouble(tokens[5]);
			}
			else
			{
				this.coresUsed = -1;
				this.percentageCoresUsed = -1;
			}

		}
		catch (NumberFormatException e)
		{
			L.error(PROCESSOR_CMD_MARKER, "Al leer la carga del procesador, no se pudo convertir el valor de uso de CPU a double.");
			L.catching(e);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el uso de CPU."));
			return false;
		}
		
		return true;
	}
	
	
	public AIXProcessorResult(OsCommandResult lparStatResult, OsCommandResult vmStatResult)
	{
		

		if (!this.processVmStatResult(vmStatResult)) return;
		if (!this.processLparStatResult(lparStatResult)) return;
		
		this.channelize();
		
		// Valores especificos AIX
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en usuario", this.userLoadPercentaje, "Percent"));
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en sistema", this.systemLoadPercentaje, "Percent"));
		this.addChannel(new PrtgChannelResult("Tiempo de CPU en iowait", this.iowaitLoadPercentaje, "Percent"));
		
		this.addChannel(new PrtgChannelResult("Cores Entitled", this.coresEntitled, "Count"));
		if (this.coresUsed != -1) this.addChannel(new PrtgChannelResult("Cores Usados", this.coresUsed, "Count"));
		if (this.percentageCoresUsed != -1) this.addChannel(new PrtgChannelResult("Porcentaje Cores Entitled", this.percentageCoresUsed, "Percent"));
		if (this.virtualCcores != -1) this.addChannel(new PrtgChannelResult("Cores Virtuales", this.virtualCcores, "Count"));
		
		this.addChannel(new PrtgChannelResult("Threads Ready (r)", this.threadsR, "Count"));
		this.addChannel(new PrtgChannelResult("Threads Waiting (b)", this.threadsB, "Count"));
		this.addChannel(new PrtgChannelResult("Threads Wait Raw (p)", this.threadsP, "Count"));
		this.addChannel(new PrtgChannelResult("Threads Wait FS (w)", this.threadsW, "Count"));
		
		

	}

}
