package es.hefame.hagent.command.os.updates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.os.updates.result.ORALNXOsUpdatesResult;

public class ORALNXOsUpdatesCommand extends OsUpdatesCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	OSUPDATES_CMD_MARKER	= MarkerManager.getMarker("OSUPDATES_CMD");

	public ORALNXOsUpdatesResult operate()
	{
		OsCommandExecutor updates_c = new OsCommandExecutor(OSUPDATES_CMD_MARKER, "yum", "check-update", "-q");

		OsCommandResult updates_r;
		try
		{
			updates_r = updates_c.run();

			ORALNXOsUpdatesResult oslevel = new ORALNXOsUpdatesResult(updates_r);
			L.trace(OSUPDATES_CMD_MARKER, "Resultado obtenido:", oslevel.jsonEncode());

			return oslevel;
		}
		catch (Exception e)
		{
			L.catching(e);
			return new ORALNXOsUpdatesResult();
		}
	}

}
