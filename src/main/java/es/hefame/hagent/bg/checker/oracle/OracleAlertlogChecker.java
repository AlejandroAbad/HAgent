package es.hefame.hagent.bg.checker.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.alert_channel.AlertChannel;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleAlertlogConfigData;
import es.hefame.hagent.util.mail.html.HtmlHeader;
import es.hefame.hagent.util.mail.html.HtmlParagraph;
import es.hefame.hagent.util.mail.html.HtmlStyler;

public class OracleAlertlogChecker extends Checker
{
	private static Logger				L						= LogManager.getLogger();

	private String						sid						= null;

	private FileReader					file_reader				= null;
	private BufferedReader				buffer					= null;
	private OracleAlertlogConfigData	config					= null;

	private Pattern						inc_pattern				= null;
	private Pattern						exc_pattern				= null;

	private Date						first_error				= null;
	private Date						flush_date				= null;
	private StringBuilder				error_buffer			= null;
	private final long					TIME_BUFFERING			= 60000;

	private String						current_alert_log_path	= "";

	public OracleAlertlogChecker(String name, String sid)
	{
		super(name, 10000);
		this.sid = sid;
	}

	private void open_file_and_read_config() throws HException
	{
		this.config = (OracleAlertlogConfigData) CONF.checker.getMonitorizedElementByName("oracle_alertlog_" + this.sid.toLowerCase());
		if (this.config == null) { throw new HException("La configuracion del elemento [oracle_alertlog_" + sid.toLowerCase() + "] no se encuentra disponible"); }

		if (this.file_reader == null || this.buffer == null || !current_alert_log_path.equals(config.get_alert_log()))
		{
			current_alert_log_path = config.get_alert_log();
			File file = new File(current_alert_log_path);
			try
			{
				this.file_reader = new FileReader(file);
				buffer = new BufferedReader(this.file_reader);
				buffer.skip(file.length());
			}
			catch (IOException e)
			{
				try
				{
					if (this.file_reader != null) this.file_reader.close();
					if (this.buffer != null) this.buffer.close();
					this.file_reader = null;
					this.buffer = null;
				}
				catch (IOException ignore)
				{
				}
				throw new HException("Error al abrir el fichero de log", e);
			}
		}
		String inc_regex = this.config.get_inc_regex();
		String exc_regex = this.config.get_exc_regex();
		if (inc_regex == null) { throw new HException("El parametro 'include_regex' para [oracle_alertlog_" + sid.toLowerCase() + "] no se encuentra disponible"); }
		this.inc_pattern = Pattern.compile(inc_regex);
		if (exc_regex != null) this.exc_pattern = Pattern.compile(exc_regex);
	}

	@Override
	public void operate() throws HException
	{
		L.trace("{} operate()", this.getClass().getSimpleName());
		if (!this.alertsEnabled())
		{
			error_buffer = null;
			first_error = null;
			flush_date = null;
			L.trace("No realizo ninguna comprobacion, pues mis alertas estan deshabilitadas");
			return;
		}

		this.open_file_and_read_config();

		String line = null;
		try
		{
			if (error_buffer == null)
			{
				error_buffer = new StringBuilder();
			}

			while ((line = buffer.readLine()) != null)
			{
				L.trace("Leida linea [{}]", line);

				if (first_error == null)
				{

					Matcher m_inc = inc_pattern.matcher(line);
					if (m_inc.find())
					{

						L.debug("La linea [{}] coincide con el patron de inclusiones", line);

						if (exc_pattern != null)
						{
							Matcher m_exc = exc_pattern.matcher(line);
							if (m_exc.find())
							{
								L.debug("La linea [{}] coincide con el patron de exclusiones. Se ignora", line);
								continue;
							}
						}
						first_error = new Date();
						flush_date = new Date(System.currentTimeMillis() + this.TIME_BUFFERING);
						L.debug("Encontrado un error a las [{}]. Se almacenaran todas las lineas hasta las [{}]", first_error, flush_date);
						error_buffer.append(line).append("<br>");
					}

				}
				else
				{
					L.debug("La linea [{}] se adjunta, pues hemos encontrado un error previo", line);
					error_buffer.append(line).append("<br>");
				}
			}

			if (flush_date != null && flush_date.before(new Date()))
			{
				L.info("Se procede a enviar una alerta [primer error = {}]", first_error);

				StringBuilder sb = new StringBuilder();

				sb.append(new HtmlStyler());
				sb.append(new HtmlHeader("Errores de log " + this.config.get_db_name(), 1, "error"));
				sb.append(new HtmlParagraph("Errores encontrados en el fichero " + this.config.get_alert_log()));
				sb.append(new HtmlParagraph("Primer error encontrado a las " + first_error.toString()));
				sb.append(new HtmlParagraph(error_buffer.toString(), "code"));
				sb.append(this.getAlertLinks());

				error_buffer = null;
				first_error = null;
				flush_date = null;

				AlertChannel channel = CONF.channels.get_channel(config.get_alert_channel());
				L.debug("El canal de alerta [{}] es {}", config.get_alert_channel(), channel.jsonEncode().toJSONString());
				channel.send("Errores en Oracle Alertlog " + this.config.get_db_name(), sb.toString());

				sb = null;

				L.info("Se detienen las alertas durante 1 hora para evitar SPAM");
				this.stop_alerting(3600);
			}

		}
		catch (IOException e)
		{

			L.catching(e);
			throw new HException("Error leyendo el fichero de log", e);
		}
	}

}
