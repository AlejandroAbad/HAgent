package es.hefame.hagent.command.apache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.Command;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hcore.http.client.HttpClient;
import es.hefame.hcore.http.client.HttpClientResponse;

public class ServerStatusCommand implements Command {

	private static Logger L = LogManager.getLogger();
	private static final Marker APACHE_SERVERSTATUS_CMD_MARKER = MarkerManager.getMarker("APACHE_SERVERSTATUS_CMD");

	private String sni;
	private boolean useHttps;

	public ServerStatusCommand(String sni, boolean useHttps) {

		this.useHttps = useHttps;

		if (sni == null || sni.trim().equals("")) {
			this.sni = AgentInfo.get_fqdn_hostname();
		} else {
			this.sni = sni.trim();
		}

	}

	public ServerStatusCommand(String sni) {
		this(sni, true);
	}

	public ServerStatusCommand() {
		this(null, true);
	}

	@Override
	public ServerStatusResult operate() {
		String serverStatusUrl = (this.useHttps ? "https" : "http") + "://" + sni + "/server-status?auto";

		try {
			HttpClient http_client = new HttpClient(serverStatusUrl);

			L.info(APACHE_SERVERSTATUS_CMD_MARKER, "Obteniendo el /server-status del servidor en la url [{}]",	serverStatusUrl);

			HttpClientResponse conn = http_client.get();

			if (conn.getStatusCode() > 399) {
				L.error(APACHE_SERVERSTATUS_CMD_MARKER, "Ocurrio un error en la llamada al estado del servidor [{} - {}]", conn.getStatusCode(), conn.getStatusCodeMessage());
				return new ServerStatusResult("Ocurrio un error al recuperar el estado de Apache");
			}

			String body_length_str = conn.getHeader("Content-length");
			if (body_length_str != null) {
				if (L.isTraceEnabled(APACHE_SERVERSTATUS_CMD_MARKER)) {
					L.trace(APACHE_SERVERSTATUS_CMD_MARKER, "La respuesta obtenida de Apache:\n{}", new String(conn.getBody()));
				}
				ServerStatusResult result = new ServerStatusResult(conn.getBody());
				return result;
			} else {
				L.error(APACHE_SERVERSTATUS_CMD_MARKER, "La respuesta obtenida de Apache no contiene datos");
				return new ServerStatusResult("Ocurrio una excepcion al recuperar el estado de Apache");
			}

		} catch (Exception e) {
			L.error(APACHE_SERVERSTATUS_CMD_MARKER, "Ocurrio una excepcion al llamar a server-status");
			L.catching(e);
			return new ServerStatusResult("Ocurrio una excepcion al recuperar el estado de Apache");
		}
	}

}
