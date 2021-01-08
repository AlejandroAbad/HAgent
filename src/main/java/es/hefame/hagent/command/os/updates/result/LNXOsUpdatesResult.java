package es.hefame.hagent.command.os.updates.result;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.simple.JSONObject;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;

public class LNXOsUpdatesResult extends OsUpdatesResult
{
	private static Logger			L						= LogManager.getLogger();
	private static final Marker		OSUPDATES_CMD_MARKER	= MarkerManager.getMarker("OSUPDATES_CMD");

	public static final DateFormat	ZYPPER_DATE_FORMAT		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected int					patches					= -1;
	protected Date					lastZypperUpdate		= null;

	public LNXOsUpdatesResult()
	{
		super();
	}

	public LNXOsUpdatesResult(OsCommandResult updates_r, OsCommandResult patches_r, OsCommandResult lastzypper_c) throws CommandException
	{
		if (updates_r.get_exit_code() > 1) { throw new CommandException("Ocurrió un error al ejecutar el comando:\n" + updates_r.toString()); }
		if (patches_r.get_exit_code() > 1) { throw new CommandException("Ocurrió un error al ejecutar el comando:\n" + patches_r.toString()); }
		if (lastzypper_c.get_exit_code() > 1) { throw new CommandException("Ocurrió un error al ejecutar el comando:\n" + lastzypper_c.toString()); }

		StringTokenizer nbTokenizer = new StringTokenizer(new String(updates_r.get_stdout()), "\r\n");
		this.updates = nbTokenizer.countTokens();

		nbTokenizer = new StringTokenizer(new String(patches_r.get_stdout()), "\r\n");
		this.patches = nbTokenizer.countTokens();

		nbTokenizer = new StringTokenizer(new String(lastzypper_c.get_stdout()));

		if (nbTokenizer.countTokens() < 2)
		{
			lastZypperUpdate = null;
		}
		else
		{
			String zypperUpdateDatetime = nbTokenizer.nextToken() + ' ' + nbTokenizer.nextToken();
			L.trace(OSUPDATES_CMD_MARKER, "Fecha leida [{}]", zypperUpdateDatetime);

			try
			{
				lastZypperUpdate = ZYPPER_DATE_FORMAT.parse(zypperUpdateDatetime);
			}
			catch (ParseException e)
			{
				throw new CommandException("No se entiende la fecha de la ultima actualizacion:\n" + lastzypper_c.toString());
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("patches", this.patches);
		root.put("lastUpdate", lastZypperUpdate == null ? null : lastZypperUpdate.getTime());
		root.put("lastUpdate2", lastZypperUpdate == null ? null : lastZypperUpdate.toString());
		return root;
	}

}
