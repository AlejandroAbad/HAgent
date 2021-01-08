package es.hefame.hagent.controller.prtg;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.RecurringOperation;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgCheckersHandler extends HttpController
{
	public static int		STATUS_OK				= 1;
	public static int		STATUS_ERROR			= 2;

	public static int		BONUS_TIME_UNTIL_DEAD	= 60 * 10 * 1000;
	private static Logger	L						= LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException
	{
		L.info("Peticion del estado de los Checkeadores para PRTG");

		RecurringOperation c = null;
		PrtgSensor sensor = new PrtgSensor();

		// ORACLE ALERTLOGS
		for (MonitorizedElementConfigData checker_config : CONF.checker.get_element_of_type("oracle_alertlog"))
		{
			try
			{
				c = (RecurringOperation) BgJobs.getJob(checker_config.get_name());
				sensor.addChannel(create_channel(checker_config.get_name(), c));
			}
			catch (ClassCastException e)
			{
				L.catching(e);
				sensor.addChannel(create_channel(checker_config.get_name(), STATUS_ERROR));
			}
		}

		// ALERTLOGS
		for (MonitorizedElementConfigData checker_config : CONF.checker.get_element_of_type("alertlog"))
		{
			try
			{
				c = (RecurringOperation) BgJobs.getJob(checker_config.get_name());
				sensor.addChannel(create_channel(checker_config.get_name(), c));
			}
			catch (ClassCastException e)
			{
				L.catching(e);
				sensor.addChannel(create_channel(checker_config.get_name(), STATUS_ERROR));
			}
		}

		// ARCHIVELOGS
		for (MonitorizedElementConfigData checker_config : CONF.checker.get_element_of_type("archivelog"))
		{
			try
			{
				c = (RecurringOperation) BgJobs.getJob(checker_config.get_name());
				sensor.addChannel(create_channel(checker_config.get_name(), c));
			}
			catch (ClassCastException e)
			{
				L.catching(e);
				sensor.addChannel(create_channel(checker_config.get_name(), STATUS_ERROR));
			}
		}

		// ERRPT
		if (CONF.checker.getMonitorizedElementByName("errpt") != null)
		{
			c = (RecurringOperation) BgJobs.getJob("errpt");
			sensor.addChannel(create_channel("errpt", c));
		}

		t.response.send(sensor, 200);
	}

	private PrtgChannelResult create_channel(String name, int value)
	{
		L.info("La comprobacion de [{}] dio resultado [{}]", name, value);
		PrtgChannelResult channel = new PrtgChannelResult(name.toUpperCase().replaceAll("\\_", " "), value, "Custom");
		channel.setValueLookup("prtg.standardlookups.yesno.stateyesok");
		return channel;
	}

	private PrtgChannelResult create_channel(String name, RecurringOperation t)
	{
		int value = STATUS_ERROR;
		if (t != null && t.isRunning() && (t.timeSinceLastSuccess() < (BONUS_TIME_UNTIL_DEAD + (1.5 * t.recurringInterval()))))
		{
			value = STATUS_OK;
		}

		return create_channel(name, value);
	}

}
