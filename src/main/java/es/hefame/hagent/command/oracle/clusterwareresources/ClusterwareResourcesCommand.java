package es.hefame.hagent.command.oracle.clusterwareresources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.oracle.clusterwareresources.result.ClusterwareResourceResult;
import es.hefame.hagent.util.exception.CommandException;

public class ClusterwareResourcesCommand implements Command
{
	private static Logger		L							= LogManager.getLogger();
	private static final Marker	OGRID_RESOUCE_CMD_MARKER	= MarkerManager.getMarker("OGRID_RESOURCE_CMD");

	@Override
	public Map<String, ClusterwareResourceResult> operate() throws HException
	{
		Map<String, ClusterwareResourceResult> results = new HashMap<String, ClusterwareResourceResult>();
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(OGRID_RESOUCE_CMD_MARKER, "su", "-", "root", "-c", "crsctl status res");
			OsCommandResult command_result = c.run();

			if (command_result.in_error())
			{
				L.error(OGRID_RESOUCE_CMD_MARKER, "El comando retorno un error: [{}]", command_result.toString());
				throw new CommandException(command_result, "Ocurrio un error al ejecutar el comando");
			}

			String[] lo_cluster_resources = new String(command_result.get_stdout()).split("\\n\\n+");

			for (String resource : lo_cluster_resources)
			{
				ClusterwareResourceResult result = new ClusterwareResourceResult(resource);
				results.put(result.get_name(), result);
			}

			return results;
		}
		catch (IOException e)
		{
			throw new CommandException("Error al ejecutar el comando", e);
		}
	}

}
