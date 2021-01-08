package es.hefame.hagent.controller.agent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AgentLog4jHandler extends HttpController
{

	private static Logger L = LogManager.getLogger();

	@Override
	public void get(HttpConnection t) throws IOException
	{
		String command = t.request.getURIField(2);

		if ("reload".equals(command))
		{
			L.info("Peticion para recargar la configuracion de Log4j");
			((LoggerContext) LogManager.getContext(false)).reconfigure();
			// t.answer(null, 200);
		}
		// else if ("marker".equals(command))
		// {
		// L.info("Peticion para leer los markers");
		// final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		// final Configuration config = ctx.getConfiguration();
		// LoggerConfig commandLoggerConfig = config.getLoggerConfig("es.hefame.hagent.command");
		//
		// CompositeFilter filters = (CompositeFilter) commandLoggerConfig.getFilter();
		//
		// for (Filter f : filters.getFiltersArray()) {
		// if (f instanceof MarkerFilter) {
		// MarkerFilter mf = (MarkerFilter) f;
		// mf.
		// }
		// }
		//
		//
		// MarkerFilter f = MarkerFilter.createFilter("OSUPDATES_CMD", Result.ACCEPT, Result.NEUTRAL);
		// filters.addFilter(f);
		//
		// ctx.updateLoggers();
		//
		// }
		else
		{
			L.info("Peticion para obtener la configuracion de Log4j");
		}

		String location = ((LoggerContext) LogManager.getContext(false)).getConfiguration().getConfigurationSource().getLocation();
		File config_file = new File(location);
		long config_file_size = config_file.length();
		char[] config_file_contents = new char[(int) config_file_size];

		FileReader reader = new FileReader(config_file);
		reader.read(config_file_contents);
		reader.close();
		t.response.send(config_file_contents, 200, "text/xml");

	}

	public void post(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion para cambiar la configuracion de Log4j");

		byte[] new_xml = t.request.getBodyAsByteArray();
		String location = ((LoggerContext) LogManager.getContext(false)).getConfiguration().getConfigurationSource().getLocation();

		L.debug("El fichero a cambiar es: [{}]", location);
		L.trace("El contenido del fichero es:\n{}", new String(new_xml, "UTF-8"));

		FileWriter writer = new FileWriter(new File(location), false);
		writer.write(new String(new_xml, "UTF-8").toCharArray());
		writer.flush();
		writer.close();

		L.debug("Recargando la configuracion de Log4j");
		((LoggerContext) LogManager.getContext(false)).reconfigure();

		char[] config_file_contents = new char[new_xml.length];
		FileReader reader = new FileReader(location);
		reader.read(config_file_contents);
		reader.close();
		t.response.send(config_file_contents, 200, "text/xml");

	}
}
