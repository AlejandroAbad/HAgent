package es.hefame.hagent.command.os.updates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.os.updates.result.LNXOsUpdatesResult;

public class LNXOsUpdatesCommand extends OsUpdatesCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	OSUPDATES_CMD_MARKER	= MarkerManager.getMarker("OSUPDATES_CMD");

	// grep 'CommitResult' /var/log/zypper.log | grep ' Commit '
	// 2018-02-02 10:24:57 <1> miespacio-pre(5663) [zypp] ZYppImpl.cc(commit):178 Commit (CommitPolicy( DownloadDefault )) returned: CommitResult (total 114, done 114, error 0, skipped 0, updateMessages 0)

	public LNXOsUpdatesResult operate()
	{
		OsCommandExecutor updates_c = new OsCommandExecutor(OSUPDATES_CMD_MARKER, "zypper", "-x", "lu").pipe("grep", "update name");
		OsCommandExecutor patches_c = new OsCommandExecutor(OSUPDATES_CMD_MARKER, "zypper", "-x", "lp").pipe("grep", "update name");
		OsCommandExecutor lastzypper_c = new OsCommandExecutor(OSUPDATES_CMD_MARKER, "grep", "CommitResult", "/var/log/zypper.log").pipe("grep", " Commit ").pipe("tail", "-1");

		OsCommandResult updates_r, patches_r, lastzypper_r;
		try
		{
			updates_r = updates_c.run();
			patches_r = patches_c.run();
			lastzypper_r = lastzypper_c.run();

			LNXOsUpdatesResult oslevel = new LNXOsUpdatesResult(updates_r, patches_r, lastzypper_r);
			L.trace(OSUPDATES_CMD_MARKER, "Resultado obtenido:", oslevel.jsonEncode());

			return oslevel;
		}
		catch (Exception e)
		{
			L.catching(e);
			return new LNXOsUpdatesResult();
		}
	}

}
