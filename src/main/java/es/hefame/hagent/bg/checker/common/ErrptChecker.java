package es.hefame.hagent.bg.checker.common;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.errpt.ErrptCommand;
import es.hefame.hagent.command.errpt.result.ErrptResult.ErrptListItem;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.alert_channel.AlertChannel;
import es.hefame.hagent.configuration.monitorized_element.common.ErrptConfigData;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hagent.util.exception.CommandException;
import es.hefame.hagent.util.mail.html.HtmlHeader;
import es.hefame.hagent.util.mail.html.HtmlStyler;
import es.hefame.hagent.util.mail.html.HtmlTable;

public class ErrptChecker extends Checker
{
	private static Logger L = LogManager.getLogger();

	public ErrptChecker()
	{
		super("errpt", 60000);
		ErrptConfigData errptConfig = (ErrptConfigData) CONF.checker.getMonitorizedElementByName("errpt");
		this.minCheckTime = errptConfig.get_check_interval();
	}

	public void operate() throws HException
	{
		ErrptCommand command = CommandFactory.new_command(ErrptCommand.class);
		List<ErrptListItem> errors = command.operate();

		if (errors != null)
		{
			if (!errors.isEmpty() && this.alertsEnabled())
			{
				String hostname = AgentInfo.get_hostname().toUpperCase();
				StringBuilder sb = new StringBuilder();
				sb.append(new HtmlStyler());
				sb.append(new HtmlHeader("Errores de ERRPT en el host " + hostname, 1, "error"));
				HtmlTable errorTable = new HtmlTable();
				errorTable.add_header("Hora", "ID", "Recurso", "Descripci&oacute;n");
				Iterator<ErrptListItem> it = errors.iterator();
				while (it.hasNext())
				{
					ErrptListItem errpt = it.next();
					errorTable.add_row(errpt.time, errpt.id, errpt.resource, errpt.description);
				}
				sb.append(errorTable);
				sb.append(this.getAlertLinks());

				ErrptConfigData errptConfig = (ErrptConfigData) CONF.checker.getMonitorizedElementByName("errpt");
				AlertChannel channel = CONF.channels.get_channel(errptConfig.get_alert_channel());
				channel.send("Errores de ERRPT en nodo " + hostname, sb.toString());

			}
			else
			{
				L.debug("No hay errores de ERRPT");
			}
		}
		else
		{
			throw new CommandException("Error al obtener la salida de errpt");
		}
	}
}
