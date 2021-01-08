package es.hefame.hagent.controller.prtg;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.process_list.ProcessListCommand;
import es.hefame.hagent.command.process_list.result.ProcessResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.common.ProcessListConfigData;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgProcessListHandler extends HttpController
{
	public static int		STATUS_OK				= 1;
	public static int		STATUS_ERROR			= 2;

	public static int		BONUS_TIME_UNTIL_DEAD	= 60 * 10 * 1000;
	private static Logger	L						= LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws HException, IOException
	{
		L.info("Peticion del estado de Procesos para PRTG");

		PrtgSensor sensor = new PrtgSensor();

		ProcessListConfigData process_config = (ProcessListConfigData) CONF.checker.getMonitorizedElementByName("process_list");

		if (process_config != null && !process_config.get_patterns().isEmpty())
		{
			for (String pattern : process_config.get_patterns())
			{
				L.debug("Checkeando procesos que se ajusten al patron [{}]", pattern);
				ProcessListCommand cmd = CommandFactory.new_command(ProcessListCommand.class, pattern);
				@SuppressWarnings("unchecked")
				Map<Integer, ProcessResult> processes = (Map<Integer, ProcessResult>) cmd.operate();

				if (L.isDebugEnabled())
				{
					for (ProcessResult pr : processes.values())
					{
						L.debug("Encontrado proceso [{}]", pr.toString());
					}
				}

				sensor.addChannel(new PrtgChannelResult(pattern, processes.size(), "Count"));

			}
		}
		else
		{
			sensor.addChannel(new PrtgErrorResult("No hay definida configuracion de procesos"));
		}
		t.response.send(sensor, 200);
	}

}
