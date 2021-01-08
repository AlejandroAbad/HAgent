package es.hefame.hagent.command.os.level;

import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.os.level.result.OsLevelResult;

public abstract class OsLevelCommand implements Command
{

	@Override
	public abstract OsLevelResult operate();
}
