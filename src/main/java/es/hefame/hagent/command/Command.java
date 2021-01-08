package es.hefame.hagent.command;

import es.hefame.hcore.HException;

public interface Command
{
	public abstract Object operate() throws HException;

}
