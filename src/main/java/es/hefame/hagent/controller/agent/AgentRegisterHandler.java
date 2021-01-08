package es.hefame.hagent.controller.agent;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hagent.util.agent.AgentInfoMessage;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AgentRegisterHandler extends HttpController
{

	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws HException, IOException
	{
		L.info("Peticion para registrar el agente");

		try
		{
			AgentInfo.register_agent();
			t.response.send(new AgentInfoMessage(), 200);
		}
		catch (HException | IOException e)
		{
			L.error("Ocurrio un error al registrar el HAgente");
			L.catching(e);
			throw e;
		}

	}

}
