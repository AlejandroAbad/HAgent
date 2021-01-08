package es.hefame.hagent.controller.prtg;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.diskpaths.DiskpathsCommand;
import es.hefame.hagent.command.diskpaths.result.DiskpathsResult;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgDiskpathsHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de los disk paths para PRTG");

		DiskpathsCommand command = CommandFactory.new_command(DiskpathsCommand.class);
		DiskpathsResult result = command.operate();

		if (result != null)
		{
			t.response.send(result, 200);
			return;
		}

		PrtgSensor error_sensor = new PrtgSensor();
		error_sensor.addChannel(new PrtgErrorResult("No hay datos de paths disponibles"));
		t.response.send(error_sensor, 200);
	}

}
