package es.hefame.hagent.command.memory;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.memory.result.MemoryResult;

public abstract class MemoryCommand implements Command
{
	@Override
	public abstract MemoryResult operate() throws HException;
}
