package es.hefame.hagent.command.process_list.result;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;

public class LNXProcessResult extends ProcessResult
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESS_LIST_CMD_MARKER	= MarkerManager.getMarker("PROCESS_LIST_CMD");

	private static int[]		CPU_TIME_MULTIPLIERS	= { 1, 60, 3600, 86400 };

	/*
	 * Min length = 8
	 * 0000|11111|2|3|44444|5|66666666|7+
	 * ----------------------------------------------------------------------------------------------------------
	 * root 10897 1 0 Feb20 ? 00:00:45 /usr/sbin/httpd-prefork -f /etc/apache2/httpd.conf
	 */

	public LNXProcessResult(String[] data) throws HException
	{
		if (data.length < 8)
		{
			L.error(PROCESS_LIST_CMD_MARKER, "No se puede parsear el proceso porque el numero de tokens es menor que 8. [{}]", (Object[]) data);
			throw new HException("Error al parsear el proceso");
		}

		this.uid = data[0];
		this.pid = Integer.parseInt(data[1]);
		this.ppid = Integer.parseInt(data[2]);
		this.start_date = data[4];

		this.cpu_time = 0;
		String times[] = data[6].split("\\:|\\-");
		for (int i = times.length - 1, j = 0; i >= 0 && j < CPU_TIME_MULTIPLIERS.length; i--, j++)
		{
			this.cpu_time += Integer.parseInt(times[i]) * CPU_TIME_MULTIPLIERS[j];
		}

		this.cmd = data[7];

		for (int i = 8; i < data.length; i++)
		{
			this.cmd += " " + data[i];
		}

	}

}
