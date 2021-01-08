package es.hefame.hagent.configuration.core;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.util.agent.AgentInfo;

public class ConfigurationReloadThread extends Thread
{
	private static Logger		L					= LogManager.getLogger();

	private boolean				avoid_next_reload	= false;
	private boolean				thread_ended		= false;
	private ConfigurationReader	config_reader		= null;

	public ConfigurationReloadThread(ConfigurationReader config_reader)
	{
		this.config_reader = config_reader;
		this.setName("ConfigReloader");
	}

	public void avoid_next_reload()
	{
		this.avoid_next_reload = true;
	}

	public void end()
	{
		this.thread_ended = true;
		this.interrupt();
	}

	@Override
	public void run()
	{
		L.info("Activada recarga automatica del fichero de configuracion");

		while (CONF.agent.configuration_reload > 0 && !thread_ended)
		{
			if (this.avoid_next_reload == true)
			{
				this.avoid_next_reload = false;
			}
			else
			{
				this.config_reader.reload();
			}

			try
			{
				AgentInfo.register_agent();
			}
			catch (IOException | HException e)
			{
				L.error("Ocurrio un error al registrar el HAgente");
				L.catching(e);
			}

			try
			{
				L.info("La configuracion se recargara en [ " + CONF.agent.configuration_reload + " ] segundos");
				Thread.sleep(CONF.agent.configuration_reload * 1000);
			}
			catch (InterruptedException e)
			{
				Thread.interrupted();
			}

		}
		L.info("La recarga automatica del fichero de configuracion se ha desactivado.");
	}
}
