package es.hefame.hagent.controller.prtg.apache;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.apache.ServerStatusCommand;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgApacheServerStatusHandler extends HttpController
{
	private static Logger		L								= LogManager.getLogger();
	private static final Marker	APACHE_SERVERSTATUS_CMD_MARKER	= MarkerManager.getMarker("APACHE_SERVERSTATUS_CMD");

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del /server-status de Apache");

		L.debug(APACHE_SERVERSTATUS_CMD_MARKER, "Lanzando ServerStatusCommand");

		String sni = t.request.getURIField(3); // Si
		String secure = t.request.getURIField(4); // Si
		
		boolean useHttps = true;
		
		if (secure != null && secure.equals("insecure")) {
			useHttps = false;
		}
		
		

		ServerStatusCommand cmd = new ServerStatusCommand(sni, useHttps);

		PrtgSensor result = cmd.operate();

		t.response.send(result, 200);
		return;
	}

}
