package es.hefame.hagent.command.diskpaths.result;

import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgSensor;
import es.hefame.hcore.prtg.PrtgThresshold;

public abstract class DiskpathsResult extends PrtgSensor
{
	// private static Logger L = LogManager.getLogger();
	// private static final Marker DISKPATHS_CMD_MARKER = MarkerManager.getMarker("DISKPATHS_CMD");

	// Kilobytes
	protected long	total_paths		= 0;
	protected long	offline_paths	= 0;
	protected long	online_paths	= 0;

	public long get_total_paths()
	{
		return total_paths;
	}

	public long get_offline_paths()
	{
		return offline_paths;
	}

	public long get_online_paths()
	{
		return online_paths;
	}

	protected void channelize()
	{
		// Thressholds
		PrtgThresshold diskpaths_error_thresshold = new PrtgThresshold(null, null, null, 0.5);

		this.addChannel(new PrtgChannelResult("Paths Total", this.get_total_paths(), "Count"));
		this.addChannel(new PrtgChannelResult("Paths Online", this.get_online_paths(), "Count"));
		this.addChannel(new PrtgChannelResult("Paths Offline", this.get_offline_paths(), "Count", diskpaths_error_thresshold));

	}

}
