package es.hefame.hagent.command.errpt;

import java.util.List;

import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.errpt.result.ErrptResult.ErrptListItem;

public abstract class ErrptCommand implements Command
{
	// private static Logger L = LogManager.getLogger();
	// private static final Marker ERRPT_CMD_MARKER = MarkerManager.getMarker("ERRPT_CMD");
	@Override
	public abstract List<ErrptListItem> operate();
}
