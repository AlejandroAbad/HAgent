package es.hefame.hagent.command;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

import es.hefame.hcore.converter.ByteArrayConverter;

public class OsCommandExecutor
{
	private static Logger			L		= LogManager.getLogger();

	private final Marker			logMarker;
	private String[]				command_exec;
	private boolean					piped	= false;
	private List<OsCommandExecutor>	pipeline;

	public OsCommandExecutor(Marker marker, String... cmd)
	{
		this.logMarker = marker;
		this.command_exec = cmd;
		this.pipeline = new LinkedList<OsCommandExecutor>();
	}

	private OsCommandExecutor(Marker marker, boolean piped, String... cmd)
	{
		this.logMarker = marker;
		this.command_exec = cmd;
		this.piped = piped;
		this.pipeline = new LinkedList<OsCommandExecutor>();
	}

	public String get_command()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.command_exec[0]);
		for (int i = 1; i < this.command_exec.length; i++)
		{
			sb.append(" \"").append(this.command_exec[i]).append("\"");
		}

		Iterator<OsCommandExecutor> it = pipeline.iterator();
		while (it.hasNext())
		{
			sb.append(" | ").append(it.next().get_command());
		}

		return sb.toString();
	}

	public OsCommandExecutor pipe(String... cmd)
	{
		pipeline.add(new OsCommandExecutor(logMarker, true, cmd));
		return this;
	}

	public OsCommandResult run() throws IOException
	{
		return this.run(new byte[0]);
	}

	public OsCommandResult run(byte[] stdin) throws IOException
	{
		if (!piped)
		{
			L.info(logMarker, "Ejecutando comando [{}]", this.get_command());
			// StdIN
			// if (L.isTraceEnabled(logMarker) && stdin != null && stdin.length > 0)
			// {
			// StringBuilder sb = new StringBuilder();
			// sb.append("Standard Input").append(OsCommandResult.LINE_HR).append("< ");
			// sb.append(new String(stdin).replaceAll("\\n", "\n< "));
			// sb.append(OsCommandResult.LINE_HR);
			// L.trace(logMarker, sb.toString());
			// }
		}
		else
		{
			L.trace(logMarker, "Lanzando comando empipado [{}]", this.get_command());
		}

		Process p = Runtime.getRuntime().exec(this.command_exec);

		// Si stdin contiene datos, los pasamos a consola.
		if (stdin != null)
		{
			L.trace(logMarker, "Mandando STDIN [ {} bytes ] al comando", stdin.length);
			OutputStream stdin_stream = p.getOutputStream();

			for (int i = 0; i < stdin.length; i++)
			{
				stdin_stream.write(stdin[i]);
				stdin_stream.flush();
			}
			stdin_stream.close();
		}

		try
		{
			byte[] out_data = ByteArrayConverter.inputStreamToBytearray(p.getInputStream());
			L.trace(logMarker, "{} bytes leidos de la salida standard.", out_data.length);
			byte[] err_data = ByteArrayConverter.inputStreamToBytearray(p.getErrorStream());
			L.trace(logMarker, "{} bytes leidos de la salida de error.", err_data.length);

			L.trace(logMarker, "Esparando que acabe la ejecucion del comando...");
			int exit_code = p.waitFor();
			L.trace(logMarker, "Ejecucion finalizada con codigo [{}], recogemos la salida.", exit_code);

			OsCommandResult result = new OsCommandResult(exit_code, out_data, err_data, stdin, this.command_exec);
			OsCommandResult last_result = result;

			Iterator<OsCommandExecutor> it = pipeline.iterator();
			while (it.hasNext())
			{
				OsCommandExecutor nextCmd = it.next();
				L.trace(logMarker, "Resultado provisional del comando [{}]", result.toString());
				L.trace(logMarker, "Lanzamos siguiente comando empipado [{}]", nextCmd.get_command());

				OsCommandResult cr = nextCmd.run(last_result.get_stdout());
				last_result = cr;
				result.pipe(cr);
			}

			if (!piped) L.debug(logMarker, "Resultado del comando:\n{}", result.toString());
			return result;
		}
		catch (InterruptedException e)
		{
			L.warn(logMarker, "El comando [{}] ha sido interrumpido", this.get_command());
			return new OsCommandResult(0xFF, stdin, this.command_exec);
		}
	}

}
