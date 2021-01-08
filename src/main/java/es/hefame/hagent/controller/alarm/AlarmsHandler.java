package es.hefame.hagent.controller.alarm;

import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AlarmsHandler extends HttpController
{
	public static final String	HANDLER_ROUTE	= "/alarm";
	private static Logger		L				= LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException
	{
		L.info("Peticion para detener una alarma");

		// Leemos el valor del nombre de la alarma (Obligatorio)
		String alarm_name = t.request.getURIField(1);
		if (alarm_name == null)
		{
			t.response.send(new HttpException(400, "El nombre de la alarma no se ha especificado"));
			return;
		}
		L.info("El nombre de la alarma es [{}]", alarm_name);

		// Leemos el valor del comando (Opcional, por defecto 'stop')
		String command = t.request.getURIField(2);
		if (command == null || command.length() == 0)
		{
			command = "stop";
			L.debug("Se utiliza el comando por defecto '{}'", command);

		}
		L.info("El comando es [{}]", command);

		// Leemos el valor de los minutos (Opcional, por defecto 720)
		int mins_to_delay = 720;
		try
		{
			String str = t.request.getURIField(3);
			if (str != null && str.length() > 0)
			{
				mins_to_delay = Integer.parseInt(t.request.getURIField(3));
			}
			else
			{
				L.debug("Se utiliza el tiempo por defecto '{}'", mins_to_delay);
			}
		}
		catch (NumberFormatException e)
		{
			L.catching(e);
			t.response.send(new HttpException(400, "El valor de tiempo no es valido", e));
			return;
		}
		L.info("El numero de minutos es [{}]", mins_to_delay);

		try
		{
			Checker c = (Checker) BgJobs.getJob(alarm_name);

			if (c != null)
			{
				AlarmStatusMessage message = null;
				switch (command)
				{
					case "stop":
						Date d = c.stop_alerting(mins_to_delay * 60);
						message = new AlarmStatusMessage(alarm_name, command, d);
						break;
					case "resume":
						c.resumeAlerting();
						message = new AlarmStatusMessage(alarm_name, command);
						break;
					default:
						t.response.send(new HttpException(404, "El comando no existe"));
				}
				t.response.send(message, 200);
			}
			else
			{
				L.warn("No se encuetra una alarma con el nombre [{}]", alarm_name);
				t.response.send(new HttpException(404, "No se encuetra una alarma con el nombre [" + alarm_name + "]"));
			}
		}
		catch (ClassCastException | NullPointerException e)
		{
			L.catching(e);
			t.response.send(new HttpException(500, "Fallo interno del servidor", e));
		}
	}

	public class AlarmStatusMessage implements JsonEncodable
	{
		private Date	end_line	= null;
		private String	command		= null;
		private String	alarm		= null;

		public AlarmStatusMessage(String alarm, String command, Date end_line)
		{
			this.alarm = alarm;
			this.command = command;
			this.end_line = end_line;
		}

		public AlarmStatusMessage(String alarm, String command)
		{
			this.alarm = alarm;
			this.command = command;
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONAware jsonEncode()
		{
			JSONObject root = new JSONObject();
			root.put("nombre_alarma", alarm);
			switch (command)
			{
				case "stop":
					root.put("mensaje", "La alarma ha sido detenida");
					if (end_line != null) root.put("silenciada_hasta", end_line);
					root.put("reinicio", "http://" + AgentInfo.get_fqdn_hostname() + ":" + CONF.agent.port + "/alarms/" + alarm + "/resume");
					break;
				case "resume":
					root.put("mensaje", "La alarma ha sido reactivada");
					break;
			}

			return root;
		}

	}

}
