package es.hefame.hagent.util.agent;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hcore.http.client.HttpClient;
import es.hefame.hcore.http.client.HttpClientResponse;

public final class AgentInfo
{
	private static Logger		L			= LogManager.getLogger();
	private static Marker		MARKER			= MarkerManager.getMarker("AGENT_INFO");

	public static final long	START_TIME	= System.currentTimeMillis();

	private static String		VERSION		= "1.20.31";
	private static String		BUILD		= "1";
	private static String		BUILT_DATE	= "20210628094300";

	/*
	static
	{
		L.debug(MARKER, "Leyendo la version del agente");

		try
		{

			Class<?> clazz = AgentInfo.class;
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			L.trace(MARKER, "El classpath es [{}]", classPath);

			String manifestPath = null;

			if (classPath.startsWith("jar"))
			{
				manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/VERSION.MF";
			}
			else
			{
				manifestPath = "rsrc:VERSION.MF";
			}

			L.debug(MARKER, "La URL del VERSION.MF calculada es {}", manifestPath);

			BufferedReader br = new BufferedReader(new InputStreamReader(new URL(manifestPath).openStream(), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				L.trace(MARKER, "Leida linea del manifest [{}]", line);
				if (line.startsWith("HAgent-Version"))
				{
					VERSION = _manifest_item_split(line);
					L.debug(MARKER, "La version es {}", VERSION);
				}
				else if (line.startsWith("HAgent-Build"))
				{
					BUILD = _manifest_item_split(line);
					L.debug(MARKER, "La build es {}", BUILD);
				}
				else if (line.startsWith("HAgent-Built-Date"))
				{
					BUILT_DATE = _manifest_item_split(line);
					L.debug(MARKER, "Fecha de compilacion {}", BUILT_DATE);
				}
			}

		}
		catch (IOException e)
		{
			L.error(MARKER, "Error al obtener la version del agente");
			L.catching(e);
		}
	}

	private static String _manifest_item_split(String line)
	{
		int separator_position = line.indexOf(':');
		if (separator_position < 0) { return ""; }

		return line.substring(separator_position + 1, line.length()).trim();
	}
	*/

	private static String hostname;

	public static String get_version()
	{
		return VERSION;
	}

	public static String get_build()
	{
		return BUILD;
	}

	public static String get_built_date()
	{
		return BUILT_DATE;
	}

	public static long get_uptime()
	{
		return System.currentTimeMillis() - START_TIME;
	}

	public static String get_fqdn_hostname()
	{
		if (AgentInfo.hostname != null) { return AgentInfo.hostname; }

		L.debug(MARKER, "Obtieniendo nombre del host");
		OsCommandExecutor cmd = new OsCommandExecutor(MARKER, "hostname");
		try
		{
			OsCommandResult result = cmd.run();
			if (!result.in_error())
			{
				String hostname = new String(result.get_stdout()).trim().toLowerCase();
				hostname = AgentInfo.add_domain(hostname);
				L.debug(MARKER, "El nombre del host es [{}]", hostname);
				AgentInfo.hostname = hostname;
			}
			else
			{
				String hostname = InetAddress.getLocalHost().getHostName();
				hostname = AgentInfo.add_domain(hostname);
				L.debug(MARKER, "El comando retorno en error. Devolvemos el hostname de la IP local [{}]", hostname);
				AgentInfo.hostname = hostname;
			}

		}
		catch (IOException e)
		{
			L.catching(e);
			AgentInfo.hostname = "";
		}
		return AgentInfo.hostname;
	}

	public static String get_hostname()
	{
		String fqdn = get_fqdn_hostname();
		String domain = '.' + CONF.agent.domain;
		if (fqdn.length() > domain.length()) { return fqdn.substring(0, fqdn.length() - domain.length()); }
		return "";
	}

	private static String add_domain(String hostname)
	{
		String domain = '.' + CONF.agent.domain;
		if (hostname.endsWith(domain)) { return hostname; }
		return hostname + domain;
	}

	public static AgentRegistrationMessage register_agent() throws IOException, HException
	{
		String registration_url = CONF.agent.registration_url;
		HttpClient http_client = new HttpClient(registration_url);

		L.info("Registrando el agente contra la url [{}]", CONF.agent.registration_url);
		L.debug("{}", () -> new AgentInfoMessage().jsonEncode().toJSONString());

		HttpClientResponse conn = http_client.post(new AgentInfoMessage().jsonEncode().toJSONString().getBytes());

		if (conn.getStatusCode() > 399) { throw new HException("El servidor retorno un error [" + conn.getStatusCode() + " - " + conn.getStatusCodeMessage() + "]"); }

		String body_length_str = conn.getHeader("Content-length");
		if (body_length_str != null)
		{
			try
			{
				return new AgentRegistrationMessage(conn.getBody());
			}
			catch (NumberFormatException e)
			{
				throw new HException("No se puede convertir el valor de 'Content-length' a entero.", e);
			}
		}
		else
		{
			throw new HttpException(400, "La respuesta del registro no contiene cabecera 'Content-length'");
		}

	}

}
