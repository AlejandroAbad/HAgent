package es.hefame.hagent.command.process_list;

import java.util.Map;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.process_list.result.ProcessResult;

public abstract class ProcessListCommand implements Command
{
	protected String cmd_filter;

	public ProcessListCommand(String cmd_filter)
	{
		this.cmd_filter = cmd_filter;
	}

	@Override
	public abstract Map<Integer, ? extends ProcessResult> operate() throws HException;
}
