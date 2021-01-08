package es.hefame.hagent.command.os.updates;

import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.os.updates.result.OsUpdatesResult;

public abstract class OsUpdatesCommand implements Command
{

	@Override
	public abstract OsUpdatesResult operate();
}
