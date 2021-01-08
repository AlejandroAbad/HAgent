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
import es.hefame.hagent.command.filesystems.result.WINFilesystemResult;

public class WINFilesystemsCommand extends FilesystemsCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	FILESYSTEMS_CMD_MARKER	= MarkerManager.getMarker("FS_CMD");

	@Override
	public Map<String, WINFilesystemResult> operate() throws HException
	{
		Map<String, WINFilesystemResult> results = new HashMap<String, WINFilesystemResult>();
		try
		{
			String[] cmd = { "powershell.exe", // Windows mola
					"get-WmiObject", "win32_logicaldisk", "-Filter", "DriveType=3", // Solo HD locales
					"|", "Select-Object", "DeviceID,FreeSpace,Size,VolumeName", // Pinta DeviceID,FreeSpace,Size
					"|", "Format-Table", "-HideTableHeaders" // Modo tabla sin cabeceras
			};

			OsCommandExecutor filesystems_cmd = new OsCommandExecutor(FILESYSTEMS_CMD_MARKER, cmd);
			OsCommandResult filesystems_cmd_result = filesystems_cmd.run();

			StringTokenizer nbTokenizer = new StringTokenizer(new String(filesystems_cmd_result.get_stdout()), "\r\n");
			String line;
			String[] tokens;

			while (nbTokenizer.hasMoreElements())
			{
				line = nbTokenizer.nextToken();
				tokens = line.split("\\s+");

				WINFilesystemResult result = new WINFilesystemResult(tokens);
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
