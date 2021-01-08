package es.hefame.hagent.command.ping;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.ping.result.PingResult;

public class PingCommand implements Command
{
	private static Logger		L				= LogManager.getLogger();
	private static final Marker	PING_CMD_MARKER	= MarkerManager.getMarker("PING_CMD");

	private String				address			= null;
	private int					timeout			= 1000;

	public PingCommand(String address)
	{
		this.address = address;
	}

	public PingCommand(String address, int timeout)
	{
		this.address = address;
		this.timeout = timeout;
	}

	public PingResult operate() throws HException
	{
		try
		{
			L.info(PING_CMD_MARKER, "Haciendo ping a la direccion [{}] con timeout [{}]", address, timeout);
			InetAddress inet = InetAddress.getByName(address);

			long time = System.nanoTime();
			boolean reached = inet.isReachable(timeout);
			double elapsed = ((double) (System.nanoTime() - time)) / 1000000.0;
			L.info(PING_CMD_MARKER, "Resultado del ping a [{}]: alcanzado [{}], tiempo [{}ms]", address, reached, elapsed);

			return new PingResult(address, reached, elapsed);
		}
		catch (IOException e)
		{
			L.error("Excepcion al alcanzar el host [{}]", address);
			L.catching(e);
			return new PingResult(address, false, -1);
		}

	}
}
