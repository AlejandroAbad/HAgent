package es.hefame.hagent.command.filesystems;

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
import es.hefame.hagent.command.filesystems.result.LNXFilesystemResult;
import es.hefame.hagent.util.exception.CommandException;

public class LNXFilesystemsCommand extends FilesystemsCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	FILESYSTEMS_CMD_MARKER	= MarkerManager.getMarker("FS_CMD");

	@Override
	public Map<String, LNXFilesystemResult> operate() throws HException
	{
		Map<String, LNXFilesystemResult> results = new HashMap<String, LNXFilesystemResult>();
		try
		{
			OsCommandExecutor c = new OsCommandExecutor(FILESYSTEMS_CMD_MARKER, "df", "-k", "-l", "-P");
			OsCommandResult cout = c.run();

			StringTokenizer nbTokenizer = new StringTokenizer(new String(cout.get_stdout()), "\n");
			String line;
			String[] tokens;

			if (nbTokenizer.countTokens() < 1) { throw new CommandException(cout, "Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " < 1]"); }

			nbTokenizer.nextToken(); // Descarta primera linea de cabecera
			while (nbTokenizer.hasMoreElements())
			{
				line = nbTokenizer.nextToken();
				tokens = line.split("\\s+");
				LNXFilesystemResult result = new LNXFilesystemResult(tokens);
				results.put(result.get_mount_point(), result);
			}

		}
		catch (Exception e)
		{
			L.catching(e);
		}

		return results;
	}

}
