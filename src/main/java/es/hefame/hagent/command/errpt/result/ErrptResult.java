package es.hefame.hagent.command.errpt.result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.util.exception.CommandException;
import es.hefame.hcore.converter.StringConverter;

public class ErrptResult
{
	private static Logger					L			= LogManager.getLogger();
	// private static final Marker ERRPT_CMD_MARKER = MarkerManager.getMarker("ERRPT_CMD");

	public static final SimpleDateFormat	DATE_FORMAT	= new SimpleDateFormat("MMddHHmmYY");

	public enum ErrptType
	{
		INFO, PERM, TEMP, UNKN;

		public static ErrptType forName(String s)
		{
			switch (s)
			{
				case "I":
					return INFO;
				case "P":
					return PERM;
				case "T":
					return TEMP;
				default:
					return UNKN;
			}
		}
	}

	public enum ErrptClass
	{
		HARDWARE, SOFTWARE, ERRLOGGER, UNKNOWN;

		public static ErrptClass forName(String s)
		{
			switch (s)
			{
				case "H":
					return HARDWARE;
				case "S":
					return SOFTWARE;
				case "O":
					return ERRLOGGER;
				default:
					return UNKNOWN;
			}
		}
	}

	private List<ErrptListItem> errpts = new LinkedList<ErrptListItem>();

	public ErrptResult(OsCommandResult result) throws CommandException
	{
		if (result.in_error()) { throw new CommandException("La ejecucion retorno un error"); }

		StringTokenizer nbTokenizer = new StringTokenizer(new String(result.get_stdout()), "\n");
		if (nbTokenizer.countTokens() < 1) { return; }

		// Descarta la primera linea que es de cabecera
		nbTokenizer.nextToken();
		while (nbTokenizer.hasMoreTokens())
		{
			String line = nbTokenizer.nextToken().trim();
			errpts.add(new ErrptListItem(line));
		}
	}

	public List<ErrptListItem> get_errpt_errors()
	{
		return this.errpts;
	}

	public class ErrptListItem
	{
		public final String 	original;
		public final String		id;
		public final Date		time;
		public final ErrptType	type;
		public final ErrptClass	cl;
		public final String		resource;
		public final String		description;

		public ErrptListItem(String line) throws CommandException
		{
			this.original = line;
			String[] tokens = line.split("\\s+");
			if (tokens.length < 6)
			{
				// La linea no cuadra
				throw new CommandException("La línea de errpt no tiene el numero de tokens adecuado");
			}

			id = tokens[0];
			try
			{
				time = DATE_FORMAT.parse(tokens[1]);
			}
			catch (ParseException e)
			{
				L.catching(e);
				throw new CommandException("El campo de fecha no tiene el formato adecuado");
			}
			type = ErrptType.forName(tokens[2]);
			cl = ErrptClass.forName(tokens[3]);
			resource = tokens[4];
			description = StringConverter.implode(" ", Arrays.copyOfRange(tokens, 5, tokens.length));
		}
	}

}
