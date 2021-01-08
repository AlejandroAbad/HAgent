package es.hefame.hagent.command.diskpaths.result;

import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class AIXDiskpathsResult extends DiskpathsResult
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	DISKPATHS_CMD_MARKER	= MarkerManager.getMarker("DISKPATHS_CMD");

	/*
	 * FORMATO ESPERADO
	 * *******************************************************************************************
	 * hdisk0:fscsi0:Available:1
	 * hdisk0:fscsi0:Available:2
	 * hdisk0:fscsi1:Available:3
	 * hdisk1:fscsi1:Available:1
	 * hdisk5:fscsi1:Available:1
	 * hdisk7:fscsi1:Available:1 ...
	 * *******************************************************************************************
	 */
	public AIXDiskpathsResult(OsCommandResult command_result)
	{
		if (command_result.in_error())
		{
			L.error(DISKPATHS_CMD_MARKER, "Ocurrio un error al ejecutar el comando. El resultado fue:\n{}", command_result.toString());
			this.addChannel(new PrtgErrorResult("No se pudo obtener el numero de paths."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\n");
		this.total_paths = 0;
		this.offline_paths = 0;

		while (nbTokenizer.hasMoreElements())
		{
			String line = nbTokenizer.nextToken().trim();
			String[] path_tokens = line.split("\\:");

			if (path_tokens.length != 4)
			{
				L.error(DISKPATHS_CMD_MARKER, "Número de tokens incorrecto al obtener datos del path [{} != 4] en la linea [{}]\n", path_tokens.length, line);
				this.addChannel(new PrtgErrorResult("No se pudo obtener el numero de paths."));
				return;
			}

			this.total_paths++;
			if (!path_tokens[2].equals("Enabled"))
			{
				L.error(DISKPATHS_CMD_MARKER, "El path {} no está 'Enabled'", (Object) path_tokens);
				this.offline_paths++;
			}

		}

		this.online_paths = this.total_paths - this.offline_paths;

		this.channelize();

	}

}
