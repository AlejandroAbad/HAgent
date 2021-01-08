package es.hefame.hagent.command.diskpaths;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.diskpaths.result.DiskpathsResult;

public abstract class DiskpathsCommand implements Command
{
	// private static Logger L = LogManager.getLogger();
	// private static final Marker DISKPATHS_CMD_MARKER = MarkerManager.getMarker("DISKPATHS_CMD");

	public abstract DiskpathsResult operate() throws HException;
}
