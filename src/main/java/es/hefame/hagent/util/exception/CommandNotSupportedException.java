package es.hefame.hagent.util.exception;

import org.json.simple.JSONObject;

import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.util.OperatingSystem;

public class CommandNotSupportedException extends HttpException
{
	private static final long				serialVersionUID	= -4615263182091852164L;
	private final Class<? extends Command>	operation;
	private final OperatingSystem			operating_system;

	public CommandNotSupportedException(Class<? extends Command> operation)
	{
		super(405, "Operacion no implementada para este SO");
		this.operating_system = OperatingSystem.get_os();
		this.operation = operation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject json_root = (JSONObject) super.jsonEncode();
		json_root.put("operation", this.operation.getSimpleName());
		json_root.put("operating_system", this.operating_system.toString());
		return json_root;
	}

}
