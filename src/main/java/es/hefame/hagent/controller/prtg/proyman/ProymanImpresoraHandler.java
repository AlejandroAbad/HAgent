package es.hefame.hagent.controller.prtg.proyman;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.checker.proyman.ProymanImpresoraChecker;
import es.hefame.hagent.bg.checker.proyman.ProymanImpresoraChecker.ImpresoraLine;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class ProymanImpresoraHandler extends HttpController
{
	public static int		STATUS_OK				= 1;
	public static int		STATUS_ERROR			= 2;

	public static int		BONUS_TIME_UNTIL_DEAD	= 60 * 10 * 1000;
	private static Logger	L						= LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException
	{
		L.info("Peticion de las líneas de la impresora de Proyman");

		ProymanImpresoraChecker checker = (ProymanImpresoraChecker) BgJobs.getJob("proyman_impresora");

		ImpresoraLine[] lines = checker.get_lines();
		PrtgSensor s = new PrtgSensor();

		String filter_type = t.request.getURIField(3);

		if (filter_type != null && filter_type.trim().length() > 0)
		{
			switch (filter_type.trim().toLowerCase())
			{
				case "server":
					String server = t.request.getURIField(4);
					if (server != null)
					{
						s = __per_server(lines, server);
					}
					else
					{
						s.addChannel(new PrtgErrorResult("El filtro 'server' debe ir junto el nombre del servidor"));
					}
					break;
				case "lines":
					s = __per_lines(lines);
					break;
				case "vlines":
					s = __general_vsNlineas(lines, 25);
					break;
				default:
					s = __general(lines);
			}
		}
		else
		{
			s = __general(lines);
		}

		t.response.send(s, 200);
	}

	private PrtgSensor __general(ImpresoraLine[] lines)
	{
		PrtgSensor sensor = new PrtgSensor();

		long time_thressh = System.currentTimeMillis() - 60000;
		int checkeos = 0;
		double checkeos_mean = 0;
		int pedidos = 0;
		double pedidos_mean = 0;
		int errores = 0;

		for (ImpresoraLine line : lines)
		{
			if (line.timestamp < time_thressh) continue;
			switch (line.type)
			{
				case CHECKEO:
					checkeos++;
					checkeos_mean += line.process_time;
					break;
				case PEDIDO:
					pedidos++;
					pedidos_mean += line.process_time;
					break;
				case ERRPEDIDO:
					errores++;
					break;
				default:
					break;
			}
		}

		if (checkeos > 0) checkeos_mean = checkeos_mean / checkeos;
		if (pedidos > 0) pedidos_mean = pedidos_mean / pedidos;

		sensor.addChannel(new PrtgChannelResult("Chequeos", checkeos, "count"));
		sensor.addChannel(new PrtgChannelResult("Pedidos", pedidos, "count"));
		sensor.addChannel(new PrtgChannelResult("Errores", errores, "count"));
		sensor.addChannel(new PrtgChannelResult("Tiempo chequeo", checkeos_mean, "TimeSeconds"));
		sensor.addChannel(new PrtgChannelResult("Tiempo pedido", pedidos_mean, "TimeSeconds"));

		return sensor;
	}

	private PrtgSensor __per_server(ImpresoraLine[] lines, String server)
	{
		PrtgSensor sensor = new PrtgSensor();

		long time_thressh = System.currentTimeMillis() - 60000;
		int checkeos = 0;
		double checkeos_mean = 0;
		int pedidos = 0;
		double pedidos_mean = 0;
		int errores = 0;

		for (ImpresoraLine line : lines)
		{
			if (!line.server.startsWith(server.toLowerCase())) continue;
			if (line.timestamp < time_thressh) continue;
			switch (line.type)
			{
				case CHECKEO:
					checkeos++;
					checkeos_mean += line.process_time;
					break;
				case PEDIDO:
					pedidos++;
					pedidos_mean += line.process_time;
					break;
				case ERRPEDIDO:
					errores++;
					break;
				default:
					break;
			}
		}

		if (checkeos > 0) checkeos_mean = checkeos_mean / checkeos;
		if (pedidos > 0) pedidos_mean = pedidos_mean / pedidos;

		sensor.addChannel(new PrtgChannelResult("Chequeos en " + server, checkeos, "count"));
		sensor.addChannel(new PrtgChannelResult("Pedidos en " + server, pedidos, "count"));
		sensor.addChannel(new PrtgChannelResult("Errores", errores, "count"));
		sensor.addChannel(new PrtgChannelResult("Tiempo chequeo en " + server, checkeos_mean, "TimeSeconds"));
		sensor.addChannel(new PrtgChannelResult("Tiempo pedido en " + server, pedidos_mean, "TimeSeconds"));

		return sensor;
	}

	private PrtgSensor __per_lines(ImpresoraLine[] lines)
	{
		PrtgSensor sensor = new PrtgSensor();

		long time_thressh = System.currentTimeMillis() - 60000;

		int numero_de_checkeos = 0;
		int numero_de_pedidos = 0;
		int total_de_lineas_en_checkeos = 0;
		int total_de_lineas_en_pedidos = 0;
		double tiempo_de_checkeo_por_linea = 0;
		double tiempo_de_pedidos_por_linea = 0;

		double a = 0;
		double b = 0;

		int errores = 0;

		for (ImpresoraLine line : lines)
		{
			if (line.timestamp < time_thressh) continue;

			switch (line.type)
			{
				case CHECKEO:
					numero_de_checkeos++;
					total_de_lineas_en_checkeos += line.no_lines;
					tiempo_de_checkeo_por_linea += line.process_time;
					a += (line.no_lines > 0) ? (line.process_time / line.no_lines) : 0;

					break;
				case PEDIDO:
					numero_de_pedidos++;
					total_de_lineas_en_pedidos += line.no_lines;
					tiempo_de_pedidos_por_linea += line.process_time;
					b += (line.no_lines > 0) ? (line.process_time / line.no_lines) : 0;

					break;
				case ERRPEDIDO:
					errores++;
					break;
				default:
					break;

			}
		}

		if (total_de_lineas_en_checkeos > 0) tiempo_de_checkeo_por_linea = tiempo_de_checkeo_por_linea / total_de_lineas_en_checkeos;
		if (total_de_lineas_en_pedidos > 0) tiempo_de_pedidos_por_linea = tiempo_de_pedidos_por_linea / total_de_lineas_en_pedidos;

		int media_de_lineas_por_chequeo = 0;
		int media_de_lineas_por_pedido = 0;
		if (numero_de_checkeos > 0) media_de_lineas_por_chequeo = total_de_lineas_en_checkeos / numero_de_checkeos;
		if (numero_de_pedidos > 0) media_de_lineas_por_pedido = total_de_lineas_en_pedidos / numero_de_pedidos;

		if (numero_de_checkeos > 0) a = a / numero_de_checkeos;
		if (numero_de_pedidos > 0) b = b / numero_de_pedidos;

		sensor.addChannel(new PrtgChannelResult("Lineas por chequeo", media_de_lineas_por_chequeo, "count"));
		sensor.addChannel(new PrtgChannelResult("Lineas por pedido", media_de_lineas_por_pedido, "count"));
		sensor.addChannel(new PrtgChannelResult("Errores", errores, "count"));
		sensor.addChannel(new PrtgChannelResult("Tiempo chequeo por linea", tiempo_de_checkeo_por_linea, "TimeSeconds"));
		sensor.addChannel(new PrtgChannelResult("Tiempo pedido por linea", tiempo_de_pedidos_por_linea, "TimeSeconds"));

		sensor.addChannel(new PrtgChannelResult("Lineas/s chequeo", a, "TimeSeconds"));
		sensor.addChannel(new PrtgChannelResult("Lineas/s pedido", b, "TimeSeconds"));

		return sensor;
	}

	private PrtgSensor __general_vsNlineas(ImpresoraLine[] lines, int lineas)
	{
		PrtgSensor sensor = new PrtgSensor();

		long time_thressh = System.currentTimeMillis() - 60000;
		int checkeos = 0;
		double checkeos_mean = 0;
		int pedidos = 0;
		double pedidos_mean = 0;

		int vcheckeos = 0;
		double vcheckeos_mean = 0;
		int vpedidos = 0;
		double vpedidos_mean = 0;

		for (ImpresoraLine line : lines)
		{
			if (line.timestamp < time_thressh) continue;
			switch (line.type)
			{
				case CHECKEO:
					checkeos++;
					checkeos_mean += line.process_time;
					if (line.no_lines < lineas)
					{
						vcheckeos++;
						vcheckeos_mean += line.process_time;
					}

					break;
				case PEDIDO:
					pedidos++;
					pedidos_mean += line.process_time;
					if (line.no_lines < lineas)
					{
						vpedidos++;
						vpedidos_mean += line.process_time;
					}

					break;
				default:
					break;
			}
		}

		if (checkeos > 0) checkeos_mean = checkeos_mean / checkeos;
		if (pedidos > 0) pedidos_mean = pedidos_mean / pedidos;

		if (vcheckeos > 0) vcheckeos_mean = vcheckeos_mean / vcheckeos;
		if (vpedidos > 0) vpedidos_mean = vpedidos_mean / vpedidos;

		sensor.addChannel(new PrtgChannelResult("Chequeos", checkeos, "count"));
		sensor.addChannel(new PrtgChannelResult("Pedidos", pedidos, "count"));
		sensor.addChannel(new PrtgChannelResult("Tiempo chequeo", checkeos_mean, "TimeSeconds"));
		sensor.addChannel(new PrtgChannelResult("Tiempo pedido", pedidos_mean, "TimeSeconds"));

		sensor.addChannel(new PrtgChannelResult("Chequeos menos " + lineas + " lineas", vcheckeos, "count"));
		sensor.addChannel(new PrtgChannelResult("Pedidos menos " + lineas + " lineas", vpedidos, "count"));
		sensor.addChannel(new PrtgChannelResult("Tiempo chequeo menos " + lineas + " lineas", vcheckeos_mean, "TimeSeconds"));
		sensor.addChannel(new PrtgChannelResult("Tiempo pedido menos " + lineas + " lineas", vpedidos_mean, "TimeSeconds"));

		return sensor;
	}

}
