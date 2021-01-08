package es.hefame.hagent.command.sap.processes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.sap.processes.result.SapProcessResult;
import es.hefame.hagent.util.exception.CommandException;

public class SapProcessListComand implements Command
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	SAP_PROCESS_CMD_MARKER	= MarkerManager.getMarker("SAP_PROCESS_CMD");

	private int					instance_number;

	public SapProcessListComand(int instance_number)
	{
		this.instance_number = instance_number;
	}

	@Override
	public List<SapProcessResult> operate() throws HException
	{
		List<SapProcessResult> results = new LinkedList<SapProcessResult>();

		try
		{
			OsCommandExecutor cmd = new OsCommandExecutor(SAP_PROCESS_CMD_MARKER, "/usr/sap/hostctrl/exe/sapcontrol", "-nr", String.valueOf(this.instance_number), "-function", "GetProcessList");
			OsCommandResult cout = cmd.run();

			StringTokenizer nbTokenizer = new StringTokenizer(new String(cout.get_stdout()), "\n");
			String line;
			String[] tokens;

			if (nbTokenizer.countTokens() < 5)
			{
				L.warn(SAP_PROCESS_CMD_MARKER, "Numero de lineas incorrecto [{} < 5]. No se hayaron procesos SAP para el numero de instancia.", nbTokenizer.countTokens());
				return results;
			}

			for (int i = 0; i < 4; i++)
				nbTokenizer.nextToken(); // Descarta primeras lineas de cabecera

			while (nbTokenizer.hasMoreElements())
			{
				line = nbTokenizer.nextToken();
				tokens = line.split(",");
				SapProcessResult result = new SapProcessResult(tokens);
				results.add(result);
			}

		}
		catch (IOException e)
		{
			L.error(SAP_PROCESS_CMD_MARKER, "Error al ejecutar el comando");
			L.catching(e);
			throw new CommandException("Error al ejecutar el comando", e);
		}

		return results;
	}

}
