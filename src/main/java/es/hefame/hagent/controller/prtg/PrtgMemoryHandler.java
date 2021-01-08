package es.hefame.hagent.controller.prtg;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.filesystems.result.FilesystemResult;
import es.hefame.hagent.command.memory.MemoryCommand;
import es.hefame.hagent.command.memory.result.MemoryResult;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgMemoryHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de la memoria para PRTG");

		MemoryCommand command = CommandFactory.new_command(MemoryCommand.class);

		MemoryResult result = command.operate();

		if (result != null)
		{
			// Incluimos /dev/shm si existe.
			Sampler s = (Sampler) BgJobs.getJob("filesystems");
			if (s != null)
			{
				@SuppressWarnings("unchecked")
				Map<String, FilesystemResult> fsresult = (Map<String, FilesystemResult>) s.getLastResult();
				if (fsresult != null)
				{
					FilesystemResult shm = fsresult.get("/dev/shm");
					if (shm != null)
					{
						result.addSensor(shm);
					}
				}
			}

			t.response.send(result, 200);
			return;
		}

		PrtgSensor error_sensor = new PrtgSensor();
		error_sensor.addChannel(new PrtgErrorResult("No hay datos de memoria disponibles"));
		t.response.send(error_sensor, 200);
	}

}
