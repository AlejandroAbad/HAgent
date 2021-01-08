package es.hefame.hagent.util.exception;

import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.command.OsCommandResult;

public class CommandException extends HttpException
{
	private OsCommandResult		command_result;

	private static final long	serialVersionUID	= -4110131431365149305L;

	public CommandException(String reason)
	{
		super(500, reason);
	}

	public CommandException(OsCommandResult command_result, String reason)
	{
		this(reason);
		this.command_result = command_result;
	}

	public CommandException(String reason, Throwable t)
	{
		super(500, reason, t);
	}

	public CommandException(OsCommandResult command_result, String reason, Throwable t)
	{
		this(reason, t);
		this.command_result = command_result;
	}

	public OsCommandResult get_command_result()
	{
		return this.command_result;
	}

}
