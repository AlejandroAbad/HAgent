package es.hefame.hagent.command.os.level.result;

import java.util.StringTokenizer;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;

public class WINOsLevelResult extends OsLevelResult
{
	private int	major;
	private int	minor;
	private int	build;
	private int	revision;

	public WINOsLevelResult()
	{
		this.major = 0;
		this.minor = 0;
		this.build = 0;
		this.revision = 0;
	}

	public WINOsLevelResult(OsCommandResult command_result) throws CommandException
	{
		if (command_result.in_error()) { throw new CommandException("Ocurrio un error al ejecutar el comando:\n" + command_result.toString()); }

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\r\n");

		if (nbTokenizer.countTokens() != 1) { throw new CommandException("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 1]\n" + command_result.toString()); }

		String line = nbTokenizer.nextToken().trim();
		String[] tokens = line.split("\\s+");

		if (tokens.length != 4) { throw new CommandException("Numero de tokens incorrecto [" + tokens.length + " != 4] en la linea [" + line + "]\n" + command_result.toString()); }

		// Major version
		try
		{
			this.major = Integer.parseInt(tokens[0]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'major version', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Minor version
		try
		{
			this.minor = Integer.parseInt(tokens[1]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'minor version', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Build
		try
		{
			this.build = Integer.parseInt(tokens[2]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'build', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Revision
		try
		{
			this.revision = Integer.parseInt(tokens[3]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'revision', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

	}

	public int get_major()
	{
		return major;
	}

	public int get_minor()
	{
		return minor;
	}

	public int get_build()
	{
		return build;
	}

	public int get_revision()
	{
		return revision;
	}

	@Override
	public String get_version()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.major).append('.').append(this.minor);
		sb.append(" build ").append(this.build);
		sb.append(" rev ").append(this.revision);
		return sb.toString();
	}

	@Override
	public int[] get_version_tokens()
	{
		int[] tokens = { this.major, this.minor, this.build, this.revision };
		return tokens;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("major", major);
		root.put("minor", minor);
		root.put("build", build);
		root.put("revision", revision);
		root.put("string", major + "." + minor + " build " + build + " rev " + revision);
		return root;
	}

}
