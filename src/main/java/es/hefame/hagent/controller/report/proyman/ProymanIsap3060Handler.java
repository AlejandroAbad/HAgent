package es.hefame.hagent.controller.report.proyman;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;

import es.hefame.hcore.http.HttpException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.checker.proyman.ProymanIsap3060Checker;
import es.hefame.hagent.bg.checker.proyman.ProymanIsap3060Checker.Isap3060Line;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class ProymanIsap3060Handler extends HttpController
{

	private static Logger L = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	@Override
	public void get(HttpConnection t) throws IOException
	{

		String werks = t.request.getURIField(3);

		L.info("Peticion de las líneas de la albaranes para el almacen [{}]", werks);

		if (werks != null && werks.trim().length() > 0)
		{
			ProymanIsap3060Checker checker = (ProymanIsap3060Checker) BgJobs.getJob("proyman_isap3060");
			List<Isap3060Line> lines = checker.getLinesForWerks(werks.trim());

			if (lines != null)
			{

				JSONArray array = new JSONArray();

				for (Isap3060Line l : lines)
				{
					array.add(l.jsonEncode());
				}

				t.response.send(array.toJSONString(), 200, "application/json");
			}
			else
			{
				HttpException error = new HttpException(404, "No se encuentra el codigo de almacen especificado");
				t.response.send(error);
			}

		}
		else
		{
			HttpException error = new HttpException(400, "No se ha especificado el codigo del almacen");
			t.response.send(error);
		}
	}

}
