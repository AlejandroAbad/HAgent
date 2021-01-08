package es.hefame.hagent.command.apache;

import java.util.StringTokenizer;

import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class ServerStatusResult extends PrtgSensor
{

	public ServerStatusResult(String error)
	{
		this.addChannel(new PrtgErrorResult(error));
	}

	public ServerStatusResult(byte[] body)
	{
		String bodyStr = new String(body);

		StringTokenizer nbTokenizer = new StringTokenizer(bodyStr, "\n");

		String threadData = null;
		while (nbTokenizer.hasMoreElements())
		{
			String line = nbTokenizer.nextToken();
			if (line.startsWith("Scoreboard:"))
			{
				threadData = line.substring(12);
				break;
			}
		}

		if (threadData == null)
		{ // No aparece la linea "Scoreboard:"
			this.addChannel(new PrtgErrorResult("No aparece el campo 'Scoreboard'"));
			return;
		}

		int ready = 0; // _
		int available = 0;// .
		int startingUp = 0;// S
		int closing = 0;// C
		int logging = 0;// L
		int finishing = 0;// G
		int idleCleanup = 0;// I
		int readingRequest = 0; // R
		int sendingReply = 0; // W
		int keepalive = 0; // K
		int dnsLookup = 0; // D

		for (int i = 0; i < threadData.length(); i++)
		{
			switch (threadData.charAt(i))
			{
				case '_':
					ready++;
					break;
				case '.':
					available++;
					break;
				case 'R':
					readingRequest++;
					break;
				case 'W':
					sendingReply++;
					break;
				case 'K':
					keepalive++;
					break;
				case 'S':
					startingUp++;
					break;
				case 'C':
					closing++;
					break;
				case 'L':
					logging++;
					break;
				case 'G':
					finishing++;
					break;
				case 'I':
					idleCleanup++;
					break;
				case 'D':
					dnsLookup++;
					break;
			}
		}

		this.addChannel(new PrtgChannelResult("Ready", ready, "Count"));
		this.addChannel(new PrtgChannelResult("Available", available, "Count"));
		this.addChannel(new PrtgChannelResult("Reading Request", readingRequest, "Count"));
		this.addChannel(new PrtgChannelResult("Sending Reply", sendingReply, "Count"));
		this.addChannel(new PrtgChannelResult("Starting Up", startingUp, "Count"));
		this.addChannel(new PrtgChannelResult("Closing", closing, "Count"));
		this.addChannel(new PrtgChannelResult("Logging", logging, "Count"));
		this.addChannel(new PrtgChannelResult("Finishing", finishing, "Count"));
		this.addChannel(new PrtgChannelResult("Idle cleanup", idleCleanup, "Count"));
		this.addChannel(new PrtgChannelResult("Keepalive", keepalive, "Count"));
		this.addChannel(new PrtgChannelResult("DNS lookup", dnsLookup, "Count"));

		int notUsedThreads = ready + available + idleCleanup;
		int usedThreads = threadData.length() - notUsedThreads;

		this.addChannel(new PrtgChannelResult("TOTAL Threads", threadData.length(), "Count"));
		this.addChannel(new PrtgChannelResult("TOTAL Used", usedThreads, "Count"));
		this.addChannel(new PrtgChannelResult("TOTAL Idle", notUsedThreads, "Count"));

	}

}
