package es.hefame.hagent.command.os.level.result;

import java.util.StringTokenizer;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;

public class LNXOsLevelResult extends OsLevelResult
{
	private int		major;
	private int		sp;
	private String	kernel;

	public LNXOsLevelResult()
	{
		this.major = 0;
		this.sp = 0;
		this.kernel = "no disponible";
	}

	public LNXOsLevelResult(OsCommandResult command_result, OsCommandResult kernel_result) throws CommandException
	{
		if (command_result.in_error()) { throw new CommandException("Ocurrio un error al ejecutar el comando:\n" + command_result.toString()); }

		if (kernel_result.in_error()) { throw new CommandException("Ocurrio un error al ejecutar el comando:\n" + kernel_result.toString()); }

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\r\n");

		if (nbTokenizer.countTokens() != 2) { throw new CommandException("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 2]\n" + command_result.toString()); }

		String line;
		String[] tokens;

		// Major version
		line = nbTokenizer.nextToken().trim();
		tokens = line.split("\\=");

		if (tokens.length != 2) { throw new CommandException("Numero de tokens incorrecto [" + tokens.length + " != 2] en la linea [" + line + "]\n" + command_result.toString()); }

		try
		{
			this.major = Integer.parseInt(tokens[1].trim());
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'major version', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Service Pack
		line = nbTokenizer.nextToken().trim();
		tokens = line.split("\\=");

		if (tokens.length != 2) { throw new CommandException("Numero de tokens incorrecto [" + tokens.length + " != 2] en la linea [" + line + "]\n" + command_result.toString()); }

		try
		{
			this.sp = Integer.parseInt(tokens[1].trim());
		}
		catch (NumberFormatException e)
		{
			throw new CommandException("Al leer el valor de 'service pack', no se pudo convertir [" + tokens[0] + "] a entero: " + e.getMessage() + "\n" + command_result.toString());
		}

		// Buscamos version del kernel
		nbTokenizer = new StringTokenizer(new String(kernel_result.get_stdout()), "\r\n");
		if (nbTokenizer.countTokens() != 1) { throw new CommandException("Numero de lineas incorrecto [" + nbTokenizer.countTokens() + " != 1]\n" + command_result.toString()); }

		this.kernel = nbTokenizer.nextToken().trim();

	}

	public int get_major()
	{
		return major;
	}

	public int get_service_pack()
	{
		return sp;
	}

	public String get_kernel()
	{
		return this.kernel;
	}

	@Override
	public String get_version()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.major).append(" SP").append(this.sp);
		sb.append(" (kernel ").append(this.kernel).append(")");
		return sb.toString();
	}

	@Override
	public int[] get_version_tokens()
	{
		int[] tokens = { this.major, this.sp };
		return tokens;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("major", major);
		root.put("sp", sp);
		root.put("kernel", kernel);
		root.put("string", major + "." + sp);
		return root;
	}
}
