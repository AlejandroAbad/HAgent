package es.hefame.hagent.controller.prtg;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.prtg.PrtgInterfacesConfiguration.InterfaceConfigData;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgInterfacesHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de los interfaces de red para PRTG");

		PrtgSensor if_sensor = new PrtgSensor();

		Iterator<InterfaceConfigData> it = CONF.prtg.interfaces.iterator();

		while (it.hasNext())
		{
			InterfaceConfigData config = it.next();
			String interface_name = config.get_interface_name();

			L.info("Leyendo informacion del interfaz [{}]", interface_name);

			PrtgSensor result = this.get_if_sensor(interface_name);

			if (result != null)
			{
				L.debug("Datos del interfaz leidos: [{}]", result.jsonEncode());
				if_sensor.addSensor(result);
			}
			else if (config.check_online())
			{
				L.warn("No hay datos del interfaz y en la configuraci�n esta marcado check_online = true");
				if_sensor.addChannel(new PrtgErrorResult("No se encuentra online el interfaz [" + config.get_interface_name() + "]"));
			}
			else
			{
				L.debug("No hay datos del interfaz");
			}
		}
		t.response.send(if_sensor, 200);
		return;
	}

	public PrtgSensor get_if_sensor(String if_name)
	{
		Sampler s = null;
		String sampler_name = "interfaces";

		s = (Sampler) BgJobs.getJob(sampler_name);

		if (s != null)
		{
			@SuppressWarnings("unchecked")
			Map<String, PrtgSensor> results = (Map<String, PrtgSensor>) s.getLastResult();
			if (results != null) { return results.get(if_name); }
		}

		L.error("El sampler [{}] no se encuentra operativo. No se pueden obtener datos de los interfaces", sampler_name);
		return null;

	}

	//
	// public void get(HttpRequest t) throws IOException, HException
	// {
	// L.info("Peticion del estado de los interfaces");
	//
	// PrtgSensor if_sensor = new PrtgSensor();
	//
	// Sampler s = (Sampler) BgJobs.get_job("interfaces");
	// if (s != null)
	// {
	// try
	// {
	// @SuppressWarnings("unchecked")
	// Map<String, PrtgInterfaceResult> results = (Map<String, PrtgInterfaceResult>) s.get_last_result();
	// if (results != null)
	// {
	// Iterator<InterfaceConfigData> it = C.prtg.interfaces.iterator();
	//
	// while (it.hasNext())
	// {
	// InterfaceConfigData config = it.next();
	// PrtgInterfaceResult result = results.get(config.get_interface_name());
	//
	// if (result != null)
	// {
	// if_sensor.add_sensor(result);
	// }
	// else if (config.check_online())
	// {
	// if_sensor.addChannel(new PrtgErrorResult("No se encuentra el interfaz [" + config.get_interface_name() + "]"));
	// }
	// }
	//
	// t.answer(if_sensor, 200);
	// return;
	// }
	//
	// }
	// catch (ClassCastException e)
	// {
	// L.catching(e);
	// }
	// }
	//
	// PrtgSensor error_sensor = new PrtgSensor();
	// error_sensor.addChannel(new PrtgErrorResult("No hay datos de filesystems disponibles"));
	// t.answer(error_sensor, 200);
	//
	// }

}
