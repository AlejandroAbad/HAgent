package es.hefame.hagent.command.interfaces;

import java.util.Map;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.interfaces.result.InterfaceResult;

public abstract class InterfacesCommand implements Command
{

	@Override
	public abstract Map<String, ? extends InterfaceResult> operate() throws HException;

}
