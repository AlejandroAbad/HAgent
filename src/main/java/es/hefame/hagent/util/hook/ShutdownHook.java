package es.hefame.hagent.util.hook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.http.server.HttpService;

public class ShutdownHook extends Thread
{
	private static Logger	L		= LogManager.getLogger();
	HttpService				server	= null;

	public ShutdownHook(HttpService server)
	{
		this.server = server;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("SHT-ShutdownHook");

		L.info("Received Shutdown signal");

		L.info("Stopping HTTP engine");
		if (server != null) server.stop(5);

		L.info("HTTP(s) server stopped");
		L.info("\n---- API stopped ----\n");

	}

}
