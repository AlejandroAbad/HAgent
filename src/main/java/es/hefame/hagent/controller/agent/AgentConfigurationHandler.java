package es.hefame.hagent.controller.agent;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AgentConfigurationHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException, HException
	{
		String command = t.request.getURIField(2);

		if ("reload".equals(command))
		{
			L.info("Peticion para recargar la configuracion del agente");
			CONF.reload();
		}
		else
		{
			L.info("Peticion para obtener la configuracion del agente");
		}
		t.response.send(CONF.instance, 200);

	}

	@Override
	public void post(HttpConnection t) throws HttpException, IOException
	{
		L.info("Peticion para establecer la configuracion del agente");
		byte[] body = t.request.getBodyAsByteArray();
		CONF.set_configuration_file_contents(body);
		t.response.send(CONF.instance, 200);
	}

}
