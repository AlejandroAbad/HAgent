package es.hefame.hagent.bg.checker.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.alert_channel.AlertChannel;
import es.hefame.hagent.configuration.monitorized_element.common.AlertlogConfigData;
import es.hefame.hagent.util.mail.html.HtmlHeader;
import es.hefame.hagent.util.mail.html.HtmlParagraph;
import es.hefame.hagent.util.mail.html.HtmlStyler;

public class AlertlogChecker extends Checker
{
	private static Logger		L				= LogManager.getLogger();
	private static DateFormat	DATE_FORMATTER	= new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' 'Z");
	private final long			TIME_BUFFERING	= 60000;

	private String				currentLogPath	= "";
	private FileReader			fileReader		= null;
	private BufferedReader		buffer			= null;
	private AlertlogConfigData	config			= null;

	private Pattern				includePattern	= null;
	private Pattern				excludePattern	= null;

	private Date				firstError		= null;
	private Date				flushDate		= null;
	private StringBuilder		errorBuffer		= null;

	public AlertlogChecker(String log_name)
	{
		super("alertlog_" + log_name.toLowerCase(), 10000);
	}

	private void openFileAndReadConfig() throws HException
	{
		this.config = (AlertlogConfigData) CONF.checker.getMonitorizedElementByName(this.getCheckerName());
		if (this.config == null) { throw new HException("La configuracion del elemento [" + this.getCheckerName() + "] no se encuentra disponible"); }

		if (this.fileReader == null || this.buffer == null || !currentLogPath.equals(config.get_alert_log()))
		{
			currentLogPath = config.get_alert_log();
			File file = new File(currentLogPath);
			try
			{
				this.fileReader = new FileReader(file);
				buffer = new BufferedReader(this.fileReader);
				buffer.skip(file.length());
			}
			catch (IOException e)
			{
				try
				{
					if (this.fileReader != null) this.fileReader.close();
					if (this.buffer != null) this.buffer.close();
					this.fileReader = null;
					this.buffer = null;
				}
				catch (IOException e1)
				{
					L.catching(e1);
				}
				throw new HException("Error al abrir el fichero de log", e);
			}
		}
		String incRegex = this.config.get_inc_regex();
		String excRegex = this.config.get_exc_regex();
		if (incRegex == null) { throw new HException("El parametro 'include_regex' para [" + this.getCheckerName() + "] no se encuentra disponible"); }
		this.includePattern = Pattern.compile(incRegex);
		if (excRegex != null) this.excludePattern = Pattern.compile(excRegex);
	}

	@Override
	public void operate() throws HException
	{
		if (!this.alertsEnabled())
		{
			errorBuffer = null;
			firstError = null;
			flushDate = null;
			L.trace("No realizo ninguna comprobacion, pues mis alertas estan deshabilitadas");
			return;
		}

		this.openFileAndReadConfig();

		String line = null;
		try
		{
			if (errorBuffer == null)
			{
				errorBuffer = new StringBuilder();
			}

			while ((line = buffer.readLine()) != null)
			{
				L.trace("Leida linea [{}]", line);

				if (firstError == null)
				{

					Matcher m_inc = includePattern.matcher(line);
					if (m_inc.find())
					{

						L.debug("La linea [{}] coincide con el patron de inclusiones", line);

						if (excludePattern != null)
						{
							Matcher m_exc = excludePattern.matcher(line);
							if (m_exc.find())
							{
								L.debug("La linea [{}] coincide con el patron de exclusiones. Se ignora", line);
								continue;
							}
						}
						firstError = new Date();
						flushDate = new Date(System.currentTimeMillis() + this.TIME_BUFFERING);
						L.debug("Encontrado un error a las [{}]. Se almacenaran todas las lineas hasta las [{}]", firstError, flushDate);
						errorBuffer.append("<b>[").append(DATE_FORMATTER.format(new Date())).append("] ").append(line).append("</b><br>");
					}

				}
				else
				{
					L.debug("La linea [{}] se adjunta, pues hemos encontrado un error previo", line);
					errorBuffer.append("[").append(DATE_FORMATTER.format(new Date())).append("] ").append(line).append("<br>");
				}
			}

			if (flushDate != null && flushDate.before(new Date()))
			{
				L.info("Enviando correo de alerta");

				StringBuilder sb = new StringBuilder();
				sb.append(new HtmlStyler());
				sb.append(new HtmlHeader("Errores de log " + this.config.get_log_name(), 1, "error"));
				sb.append(new HtmlParagraph("Errores encontrados en el fichero " + this.config.get_alert_log()));
				sb.append(new HtmlParagraph("Primer error encontrado a las " + firstError.toString()));
				sb.append(new HtmlParagraph(errorBuffer.toString(), "code"));
				sb.append(this.getAlertLinks());

				errorBuffer = null;
				firstError = null;
				flushDate = null;

				AlertChannel channel = CONF.channels.get_channel(config.get_alert_channel());
				channel.send("Errores en el log " + this.config.get_log_name(), sb.toString());

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
