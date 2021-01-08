package es.hefame.hagent.command.ping.result;

import es.hefame.hcore.prtg.PrtgChannelResult;

public class PingResult extends PrtgChannelResult
{

	public PingResult(String host, boolean reached, double miliSeconds)
	{
		super(host, (reached ? miliSeconds : -1), "ms");
	}

}
