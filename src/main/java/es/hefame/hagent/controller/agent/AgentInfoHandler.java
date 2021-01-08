package es.hefame.hagent.controller.agent;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.util.agent.AgentInfoMessage;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AgentInfoHandler extends HttpController
{

	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException
	{
		L.info("Peticion para obtener informacion del agente");
		t.response.send(new AgentInfoMessage(), 200);
	}

}
