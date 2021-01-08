package es.hefame.hagent.command.os.updates.result;

import java.util.StringTokenizer;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;

public class ORALNXOsUpdatesResult extends OsUpdatesResult
{

	public ORALNXOsUpdatesResult()
	{
		super();
	}

	public ORALNXOsUpdatesResult(OsCommandResult updates_r) throws CommandException
	{
		if (updates_r.in_error(100)) { throw new CommandException("Ocurrió un error al ejecutar el comando:\n" + updates_r.toString()); }

		StringTokenizer nbTokenizer = new StringTokenizer(new String(updates_r.get_stdout()), "\r\n");
		this.updates = nbTokenizer.countTokens() - 1;

	}

}
