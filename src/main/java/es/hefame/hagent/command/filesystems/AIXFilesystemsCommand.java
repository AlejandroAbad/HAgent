package es.hefame.hagent.command.filesystems;

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
import es.hefame.hagent.command.filesystems.result.AIXFilesystemResult;
import es.hefame.hagent.util.exception.CommandException;

public class AIXFilesystemsCommand extends FilesystemsCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	FILESYSTEMS_CMD_MARKER	= MarkerManager.getMarker("FS_CMD");

	@Override
	public Map<String, AIXFilesystemResult> operate() throws HException
	{
		Map<String, AIXFilesystemResult> results = new HashMap<String, AIXFilesystemResult>();
		try
		{
			OsCommandExecutor filesystems_cmd = new OsCommandExecutor(FILESYSTEMS_CMD_MARKER, "df", "-k", "-i", "-M", "-v");
			OsCommandResult filesystems_cmd_result = filesystems_cmd.run();

			if (filesystems_cmd_result.in_error()) { throw new CommandException(filesystems_cmd_result, "Ocurrio un error al ejecutar el comando"); }

			StringTokenizer nbTokenizer = new StringTokenizer(new String(filesystems_cmd_result.get_stdout()), "\n");

			if (nbTokenizer.countTokens() < 1) { throw new CommandException(filesystems_cmd_result, "Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " < 1]"); }

			String line;
			String[] tokens;
			nbTokenizer.nextToken(); // Descarta primera linea de cabecera
			while (nbTokenizer.hasMoreElements())
			{
				line = nbTokenizer.nextToken();
				tokens = line.split("\\s+");
				AIXFilesystemResult result = new AIXFilesystemResult(tokens);
				results.put(result.get_mount_point(), result);
			}

		}
		catch (IOException | CommandException e)
		{
			L.catching(e);
		}
		return results;

	}

}
