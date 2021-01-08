package es.hefame.hagent.command.os.level.result;

import java.util.StringTokenizer;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;

public class AIXOsLevelResult extends OsLevelResult
{
	private int	major;
	private int	tl;
	private int	sp;
	private int	date;

	public AIXOsLevelResult()
	{
		this.major = 0;
		this.tl = 0;
		this.sp = 0;
		this.date = 0;
	}

	public AIXOsLevelResult(OsCommandResult command_result) throws CommandException
	{
		if (command_result.in_error()) { throw new CommandException("Ocurrio un error al ejecutar el comando:\n" + command_result.toString()); }

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\r\n");

		if (nbTokenizer.countTokens() != 1) { throw new CommandException("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 1]\n" + command_result.toString()); }

		String line = nbTokenizer.nextToken().trim();
		String[] tokens = line.split("-");

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
			this.tl = Integer.parseInt(tokens[1]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'tech level', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Build
		try
		{
			this.sp = Integer.parseInt(tokens[2]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'service pack', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Revision
		try
		{
			this.date = Integer.parseInt(tokens[3]);
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'date', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

	}

	public int get_major()
	{
		return major;
	}

	public int get_technology_level()
	{
		return tl;
	}

	public int get_service_pack()
	{
		return sp;
	}

	public int get_date()
	{
		return date;
	}

	@Override
	public String get_version()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.major).append("-0").append(this.tl).append("-0").append(this.sp).append("-").append(this.date);
		return sb.toString();
	}

	@Override
	public int[] get_version_tokens()
	{
		int[] tokens = { this.major, this.sp, this.tl, this.date };
		return tokens;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("major", major);
		root.put("sp", sp);
		root.put("tl", tl);
		root.put("date", date);
		root.put("string", major + " TL" + tl + " SP" + sp + "(" + date + ")");
		return root;
	}
}
