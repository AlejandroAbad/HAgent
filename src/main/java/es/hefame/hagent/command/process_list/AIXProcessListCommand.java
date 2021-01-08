package es.hefame.hagent.command.process_list;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.process_list.result.AIXProcessResult;

public class AIXProcessListCommand extends ProcessListCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESS_LIST_CMD_MARKER	= MarkerManager.getMarker("PROCESS_LIST_CMD");

	public AIXProcessListCommand(String cmd_filter)
	{
		super(cmd_filter);
	}

	@Override
	public Map<Integer, AIXProcessResult> operate() throws HException
	{
		Map<Integer, AIXProcessResult> results = new HashMap<Integer, AIXProcessResult>();

		try
		{
			OsCommandExecutor cmd = new OsCommandExecutor(PROCESS_LIST_CMD_MARKER, "ps", "-ef");

			if (this.cmd_filter != null)
			{
				cmd.pipe("grep", cmd_filter).pipe("grep", "-v", "grep");
			}

			OsCommandResult os_result = cmd.run();
			L.debug(PROCESS_LIST_CMD_MARKER, "Resultado de la ejecucion [{}]", os_result);

			StringTokenizer nbTokenizer = new StringTokenizer(new String(os_result.get_stdout()), "\n");
			String line;
			String[] tokens;

			if (nbTokenizer.countTokens() < 1) { return results; }

			while (nbTokenizer.hasMoreElements())
			{
				line = nbTokenizer.nextToken().trim();
				tokens = line.split("\\s+");
				AIXProcessResult result = new AIXProcessResult(tokens);
				results.put(result.get_pid(), result);
			}
			return results;

		}
		catch (IOException e)
		{

			throw new HException("Error al ejecutar el comando", e);
		}
	}
}
