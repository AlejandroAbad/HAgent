package es.hefame.hagent.controller.prtg.sap;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.command.sap.processes.SapProcessListComand;
import es.hefame.hagent.command.sap.processes.result.SapProcessResult;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgSapProcessesHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de los procesos SAP");

		PrtgSensor sensor = new PrtgSensor();

		int param_number = 3;
		String param;
		while ((param = t.request.getURIField(param_number)) != null)
		{
			int instance_number = 0;
			if (param != null)
			{
				try
				{
					instance_number = Integer.parseInt(param);
					if (instance_number >= 0 && instance_number < 100)
					{
						L.info("Se solicita informacion para la instancia SAP [{}]", instance_number);
					}
					else
					{
						L.error("El valor [{}] no es un numero de instancia valido", instance_number);
						throw new HttpException(400, "Numero de instancia SAP incorrecto");
					}
				}
				catch (NumberFormatException e)
				{
					L.error("No se pudo convertir [{}] a numero");
					L.catching(e);
					throw new HttpException(400, "Numero de instancia SAP incorrecto");
				}
			}

			SapProcessListComand command = new SapProcessListComand(instance_number);
			List<SapProcessResult> results = command.operate();

			if (results != null && !results.isEmpty())
			{
				for (SapProcessResult process : results)
				{
					L.info("Evaluando estado del proceso [{}]", process.get_name());
					sensor.addSensor(process);
				}
				t.response.send(sensor, 200);
				return;
			}
			param_number++;
		}

		sensor.addChannel(new PrtgErrorResult("No se encontraron procesos SAP"));
		t.response.send(sensor, 200);
		return;
	}

}
