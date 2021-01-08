package es.hefame.hagent.command.processor;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.processor.result.ProcessorResult;

public abstract class ProcessorCommand implements Command
{

	@Override
	public abstract ProcessorResult operate() throws HException;

}
