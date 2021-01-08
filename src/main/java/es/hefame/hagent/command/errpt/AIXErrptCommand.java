package es.hefame.hagent.command.errpt;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.errpt.result.ErrptResult;
import es.hefame.hagent.command.errpt.result.ErrptResult.ErrptListItem;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.common.ErrptConfigData;
import es.hefame.hagent.util.exception.CommandException;

public class AIXErrptCommand extends ErrptCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	ERRPT_CMD_MARKER	= MarkerManager.getMarker("ERRPT_CMD");

	private static Date			last_sample			= null;

	@Override
	public List<ErrptListItem> operate()
	{
		if (last_sample == null) last_sample = new Date();

		try
		{
			ErrptConfigData ecd = (ErrptConfigData) CONF.checker.getMonitorizedElementByName("errpt");

			String errpt_types_filter = ecd.get_type_filter();

			OsCommandExecutor errpt_cmd = new OsCommandExecutor(ERRPT_CMD_MARKER, "errpt", "-s", ErrptResult.DATE_FORMAT.format(last_sample), "-T", errpt_types_filter);
			OsCommandResult errpt_cmd_result = errpt_cmd.run();

			ErrptResult result = new ErrptResult(errpt_cmd_result);

			List<ErrptListItem> errpt_errors = result.get_errpt_errors();

			/*
			 * if (errpt_errors.size() > 0)
			 * {
			 * errpt_cmd = new OsCommandExecutor("errpt", "-a", "-s", ErrptResult.DATE_FORMAT.format(last_sample), "-T", "INFO,PEND,PERF,PERM,TEMP,UNKN");
			 * errpt_cmd_result = errpt_cmd.run();
			 * }
			 */

			last_sample = new Date(System.currentTimeMillis() + (60000));
			return errpt_errors;

		}
		catch (CommandException | IOException e)
		{
			L.catching(e);
		}
		return null;
	}

}
