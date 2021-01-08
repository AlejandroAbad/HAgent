package es.hefame.hagent.controller.agent;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.restart.RestartCommand;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AgentRestartHandler extends HttpController
{

	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion para reiniciar el agente");
		t.response.send(new JsonEncodable()
		{
			@SuppressWarnings("unchecked")
			@Override
			public JSONObject jsonEncode()
			{
				JSONObject obj = new JSONObject();
				obj.put("message", "Iniciado reinicio del HAgente");
				obj.put("date", System.currentTimeMillis());
				return obj;
			}
		}, 202);

		RestartCommand command = CommandFactory.new_command(RestartCommand.class);
		command.operate();
	}

}
