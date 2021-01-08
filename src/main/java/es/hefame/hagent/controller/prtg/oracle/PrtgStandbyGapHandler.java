package es.hefame.hagent.controller.prtg.oracle;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.oracle.stb.StandbyGapCommand;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgStandbyGapHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion para calcular el GAP entre la STB database y su primary");

		StandbyGapCommand cmd = new StandbyGapCommand();
		PrtgSensor s = cmd.operate();
		t.response.send(s, 200);

	}

}
