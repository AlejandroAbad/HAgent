package es.hefame.hagent.command.process_list.result;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;

public class AIXProcessResult extends ProcessResult
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESS_LIST_CMD_MARKER	= MarkerManager.getMarker("PROCESS_LIST_CMD");

	private static int[]		CPU_TIME_MULTIPLIERS	= { 1, 60, 3600, 86400 };

	/*
	 * 0-----|1------|2-------|3|4--|5-|6----|7---|8+
	 * ----------------------------------------------------------------------------------------------------------
	 * daaadm 9109524 16973888 0 Mar 10 pts/0 0:00 vi dev_jstart
	 * 
	 * or
	 * 
	 * 0---|1-------|2------|3|4-------|5|6---|7+
	 * ----------------------------------------------------------------------------------------------------------
	 * root 12255344 2339638 0 20:08:24 - 0:00 vmstat 60 1
	 */

	public AIXProcessResult(String[] data) throws HException
	{
		if (data.length < 8)
		{
			L.error(PROCESS_LIST_CMD_MARKER, "No se puede parsear el proceso porque el numero de tokens es menor que 8. {}{}", "", data);
			throw new HException("Error al parsear el proceso");
		}

		this.uid = data[0];
		this.pid = Integer.parseInt(data[1]);
		this.ppid = Integer.parseInt(data[2]);

		int next_idx = 6;
		try
		{
			// Caso 1
			Integer.parseInt(data[5]);
			this.start_date = data[4] + data[5];
			next_idx = 7;
		}
		catch (NumberFormatException e)
		{
			// Caso 2
			this.start_date = data[4];
			next_idx = 6;
		}

		this.cpu_time = 0;
		String times[] = data[next_idx].split("\\:|\\-");
		for (int i = times.length - 1, j = 0; i >= 0 && j < CPU_TIME_MULTIPLIERS.length; i--, j++)
		{
			this.cpu_time += Integer.parseInt(times[i]) * CPU_TIME_MULTIPLIERS[j];
		}

		this.cmd = data[next_idx + 1];

		for (int i = next_idx + 2; i < data.length; i++)
		{
			this.cmd += " " + data[i];
		}

	}

}
