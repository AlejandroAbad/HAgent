package es.hefame.hagent.command;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OsCommandResult
{
	public static String				LINE_HR	= "\n-------------------------------------------------------------------------------------------\n";

	private final byte[]				stdout;
	private final byte[]				stderr;
	private final byte[]				stdin;
	private final int					exit_code;
	private final String[]				command;
	private final List<OsCommandResult>	pipeline;
	private OsCommandResult				last;

	public OsCommandResult(int exitCode, byte[] stdout, byte[] stderr, byte[] stdin, String[] command)
	{
		this.stdout = stdout;
		this.stderr = stderr;
		this.stdin = stdin;
		this.exit_code = exitCode;
		this.command = command;
		this.pipeline = new LinkedList<OsCommandResult>();
		this.last = this;
	}

	public OsCommandResult(int exitCode, byte[] stdin, String[] command)
	{
		this(exitCode, new byte[0], new byte[0], stdin, command);
	}

	public byte[] get_stdout()
	{
		return last.stdout;
	}

	public byte[] get_stderr()
	{
		return last.stderr;
	}

	public byte[] get_stdin()
	{
		return last.stdin;
	}

	public int get_exit_code()
	{
		return last.exit_code;
	}

	public String[] get_command()
	{
		return this.command;
	}

	public boolean in_error(Integer... accepted_rcs)
	{
		boolean error = ((this.stderr.length > 0) || (this.exit_code != 0));

		if (accepted_rcs != null && accepted_rcs.length > 0)
		{
			for (Integer acc_rc : accepted_rcs)
			{
				if (this.exit_code == acc_rc)
				{
					error = (this.stderr.length > 0);
				}
			}
		}

		Iterator<OsCommandResult> it = this.pipeline.iterator();
		while (it.hasNext() && !error)
		{
			error |= it.next().in_error();
		}

		return error;
	}

	public void pipe(OsCommandResult result)
	{
		this.pipeline.add(result);
		this.last = result;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append(" [");

		// Command
		if (command != null && command.length > 0)
		{
			sb.append("\nCommand: ").append(command[0]);
			for (int i = 1; i < command.length; i++)
			{
				sb.append(" \"").append(command[i]).append("\"");
			}
		}

		// StdIN
		if (this.stdin != null && this.stdin.length > 0)
		{
			sb.append("\nStandard Input:").append(LINE_HR).append("< ");
			sb.append(new String(this.stdin).replaceAll("\\n", "\n< "));
			sb.append(LINE_HR);
		}

		// Exit Code
		sb.append("\nExitCode: ").append(this.exit_code);

		// StdOUT
		if (this.stdout != null && this.stdout.length > 0)
		{
			sb.append("\nStandard Output:").append(LINE_HR).append("> ");
			sb.append(new String(this.stdout).replaceAll("\\n", "\n> "));
			sb.append(LINE_HR);
		}

		// StdError
		if (this.stderr != null && this.stderr.length > 0)
		{
			sb.append("\n\tStandard Error:").append(LINE_HR).append("# ");
			sb.append(new String(this.stderr).replaceAll("\\n", "\n# "));
			sb.append(LINE_HR);
		}

		int index = 1;
		String tab = "";
		Iterator<OsCommandResult> it = this.pipeline.iterator();
		while (it.hasNext())
		{
			tab += "\t";
			sb.append("\n").append(tab).append("[PIPE #").append(index++).append("] -> ");
			sb.append(it.next().toString().replaceAll("\\n", "\n" + tab));
		}
		return sb.toString();
	}

}
