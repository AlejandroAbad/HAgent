package es.hefame.hagent.controller.prtg;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.command.processor.result.ProcessorResult;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgProcessorHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de la CPU para PRTG");

		Sampler s = (Sampler) BgJobs.getJob("processor");
		if (s != null)
		{
			ProcessorResult result = (ProcessorResult) s.getLastResult();

			if (result != null)
			{
				t.response.send(result, 200);
				return;
			} else {
				result = new ProcessorResult(){};
				t.response.send(result, 200);
				return;
			}
		}

		PrtgSensor cpu_sensor = new PrtgSensor();
		cpu_sensor.addChannel(new PrtgErrorResult("No hay datos del procesador disponibles"));
		t.response.send(cpu_sensor, 200);

	}

}
