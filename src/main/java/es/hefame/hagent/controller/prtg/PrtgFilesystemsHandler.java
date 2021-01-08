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
import es.hefame.hagent.configuration.prtg.PrtgFilesystemsConfiguration.FilesystemConfigData;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgFilesystemsHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de los filesystems para PRTG");

		boolean retrieve_cluster_fs_only = false;

		String nd_field = t.request.getURIField(2);
		if (nd_field != null && nd_field.equalsIgnoreCase("cluster"))
		{
			L.info("La peticion es para los filesystems marcados como cluster");
			retrieve_cluster_fs_only = true;
		}

		PrtgSensor fs_sensor = new PrtgSensor();

		Iterator<FilesystemConfigData> it = CONF.prtg.filesystems.iterator();

		while (it.hasNext())
		{
			FilesystemConfigData config = it.next();

			if (config.is_cluster_fs() != retrieve_cluster_fs_only)
			{
				continue;
			}

			String mount_point = config.get_mount_point();

			L.info("Leyendo informacion del filesystem [{}]", mount_point);

			PrtgSensor result = this.get_fs_sensor(mount_point);

			if (result != null)
			{
				L.debug("Datos del filesystem leidos: [{}]", result.jsonEncode());
				fs_sensor.addSensor(result);
			}
			else if (config.check_mounted())
			{
				L.warn("No hay datos del filesystem y en la configuracion esta marcado check_mounted = true");
				fs_sensor.addChannel(new PrtgErrorResult("No se encuentra montado el filesystem [" + config.get_mount_point() + "]"));
			}
			else
			{
				L.debug("No hay datos del filesystem");
			}
		}
		t.response.send(fs_sensor, 200);
		return;
	}

	public PrtgSensor get_fs_sensor(String mount_point)
	{
		Sampler s = null;
		String sampler_name;
		if (mount_point.charAt(0) == '+')
		{
			sampler_name = "asm_diskgroups";
		}
		else
		{
			sampler_name = "filesystems";
		}

		s = (Sampler) BgJobs.getJob(sampler_name);

		if (s != null)
		{
			@SuppressWarnings("unchecked")
			Map<String, PrtgSensor> results = (Map<String, PrtgSensor>) s.getLastResult();
			if (results != null) { return results.get(mount_point); }
		}

		L.error("El sampler [{}] no se encuentra operativo. No se pueden obtener datos de los filesystems", sampler_name);
		return null;

	}

}
