package es.hefame.hagent.util.exception;

import org.json.simple.JSONObject;

import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.command.Command;

public class CommandUnavailableException extends HttpException
{
	private static final long				serialVersionUID	= -4615263182091852164L;
	private final Class<? extends Command>	operation_class;

	public CommandUnavailableException(Class<? extends Command> operation_type)
	{
		super(404, "Operacion no disponible ahora mismo.");
		this.operation_class = operation_type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject json_root = (JSONObject) super.jsonEncode();
		json_root.put("operation", this.operation_class.getSimpleName());
		return json_root;
	}

}
