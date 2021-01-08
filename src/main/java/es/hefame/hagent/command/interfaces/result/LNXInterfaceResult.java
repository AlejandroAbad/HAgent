package es.hefame.hagent.command.interfaces.result;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class LNXInterfaceResult extends InterfaceResult
{
	private static Logger		L				= LogManager.getLogger();
	private static final Marker	IF_CMD_MARKER	= MarkerManager.getMarker("IF_CMD");

	private long				rx_bytes		= 0;
	private long				tx_bytes		= 0;
	private long				timestamp		= 0;

	/*
	 * FORMATO ESPERADO
	 * ********************************************************************************
	 * collisions:0
	 * multicast:2415
	 * rx_bytes:14657398529
	 * rx_compressed:0
	 * rx_crc_errors:0
	 * rx_dropped:10
	 * rx_errors:0
	 * rx_fifo_errors:0
	 * rx_frame_errors:0
	 * rx_length_errors:0
	 * rx_missed_errors:0
	 * rx_over_errors:0
	 * rx_packets:17466465
	 * tx_aborted_errors:0
	 * tx_bytes:10232396141
	 * tx_carrier_errors:0
	 * tx_compressed:0
	 * tx_dropped:0
	 * tx_errors:0
	 * tx_fifo_errors:0
	 * tx_heartbeat_errors:0
	 * tx_packets:13955492
	 * tx_window_errors:0
	 * ********************************************************************************
	 */

	public LNXInterfaceResult(String interface_name, OsCommandResult command_result)
	{

		this.interface_name = interface_name;
		if (command_result.in_error())
		{
			L.error(IF_CMD_MARKER, "Ocurrio un error al ejecutar el comando [{}]", command_result);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el interfaz."));
			return;
		}

		StringTokenizer nbTokenizer = new StringTokenizer(new String(command_result.get_stdout()), "\n");
		Map<String, Long> iface_data = new HashMap<String, Long>(nbTokenizer.countTokens());

		while (nbTokenizer.hasMoreTokens())
		{
			String line = nbTokenizer.nextToken();
			String[] tokens = line.split(":");

			if (tokens.length != 2)
			{
				L.error(IF_CMD_MARKER, "Linea descartada [{}] por no contener 2 tokens", line);
			}
			else
			{
				try
				{
					iface_data.put(tokens[0].trim(), Long.parseLong(tokens[1]));
				}
				catch (NumberFormatException e)
				{
					L.error(IF_CMD_MARKER, "Linea descartada [{}] por una excepcion", line);
					L.catching(e);
				}
			}
		}

		this.timestamp = System.currentTimeMillis();
		this.rx_bytes = iface_data.get("rx_bytes");
		this.tx_bytes = iface_data.get("tx_bytes");

		// Recuperamos el ultimo resultado
		long last_rx = 0;
		long last_tx = 0;
		long last_timestamp = 0;

		Sampler s = (Sampler) BgJobs.getJob("interfaces");
		if (s != null)
		{
			try
			{
				@SuppressWarnings("unchecked")
				Map<String, LNXInterfaceResult> results = (Map<String, LNXInterfaceResult>) s.getLastResult();
				if (results != null)
				{
					LNXInterfaceResult last_result = results.get(this.interface_name);
					if (last_result != null)
					{
						last_rx = last_result.rx_bytes;
						last_tx = last_result.tx_bytes;
						last_timestamp = last_result.timestamp;
					}
				}
			}
			catch (ClassCastException e)
			{
				L.catching(e);
			}
		}
		else
		{
			L.debug(IF_CMD_MARKER, "No se encuentra el sampler disponible, los datos son desde el principio de los tiempos");
		}

		// Ya tenemos el ultimo resultado. Ahora hacemos los calculos pertinentes
		long diff_rx = rx_bytes - last_rx;
		long diff_tx = tx_bytes - last_tx;
		double delta_time = ((double) (timestamp - last_timestamp)) / 1000;

		if (delta_time > 0)
		{
			this.rx_bytes_per_second = (long) (diff_rx / delta_time);
			this.tx_bytes_per_second = (long) (diff_tx / delta_time);
		}
		else
		{
			this.rx_bytes_per_second = 0;
			this.tx_bytes_per_second = 0;
		}

		this.total_bytes_per_second = this.rx_bytes_per_second + this.tx_bytes_per_second;

		this.channelize();

	}

}
