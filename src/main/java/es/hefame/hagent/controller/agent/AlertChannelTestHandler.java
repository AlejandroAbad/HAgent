package es.hefame.hagent.controller.agent;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.alert_channel.AlertChannel;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AlertChannelTestHandler extends HttpController
{

	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion para probar el servicio de correo");

		String channel_name = t.request.getURIField(2);

		if (channel_name != null)
		{
			L.info("Se pide usar el canal de alertas con nombre [{}]", channel_name);
		}
		else
		{
			L.info("Se usa el canal de alertas [default] por omision");
		}

		AlertChannel channel = CONF.channels.get_channel(channel_name);
		L.info("Se utilizara el canal [{}]", channel.jsonEncode().toJSONString());

		// -----------------------------------------------

		channel.send("Mensaje de prueba desde " + AgentInfo.get_fqdn_hostname(), "Mensaje de prueba desde " + AgentInfo.get_fqdn_hostname());

		t.response.send(null, 202);
	}

}
