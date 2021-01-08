package es.hefame.hagent.command.restart;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;

public abstract class RestartCommand implements Command
{

	@Override
	public abstract Object operate() throws HException;

}
