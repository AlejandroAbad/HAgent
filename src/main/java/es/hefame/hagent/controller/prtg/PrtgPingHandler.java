package es.hefame.hagent.controller.prtg;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.ping.PingCommand;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgPingHandler extends HttpController
{
	private static Logger		L				= LogManager.getLogger();
	private static final Marker	PING_CMD_MARKER	= MarkerManager.getMarker("PING_CMD");

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion de hacer Ping a otros equipos");

		PrtgSensor sensor = new PrtgSensor();

		int param_number = 2;
		String remoteHost;
		while ((remoteHost = t.request.getURIField(param_number)) != null)
		{
			L.debug(PING_CMD_MARKER, "Lanzando PingCommand para el host [{}]. Parametro #{} en URL.", remoteHost, param_number);
			PingCommand cmd = new PingCommand(remoteHost);
			sensor.addChannel(cmd.operate());
			param_number++;
		}

		t.response.send(sensor, 200);
		return;
	}

}
