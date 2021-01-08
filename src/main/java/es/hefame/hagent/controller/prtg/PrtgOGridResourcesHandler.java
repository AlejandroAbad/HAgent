package es.hefame.hagent.controller.prtg;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.oracle.clusterwareresources.ClusterwareResourcesCommand;
import es.hefame.hagent.command.oracle.clusterwareresources.result.ClusterwareResourceResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleGridResourcesConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleGridResourcesConfigData.OracleGridResourceCondition;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class PrtgOGridResourcesHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();

	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Peticion del estado de los recursos de Oracle GRID para PRTG");

		ClusterwareResourcesCommand command = new ClusterwareResourcesCommand();

		Map<String, ClusterwareResourceResult> results = command.operate();
		PrtgSensor sensor = new PrtgSensor();

		OracleGridResourcesConfigData ogrid_res = (OracleGridResourcesConfigData) CONF.checker.getMonitorizedElementByName("oracle_grid_resources");

		if (ogrid_res != null)
		{
			for (ClusterwareResourceResult resource : results.values())
			{
				L.debug("Evaluando estado del recurso [{}][{}]", resource.get_name(), resource.get_type());

				OracleGridResourceCondition location_condition = ogrid_res.get_resource_condition(resource.get_name());

				if (location_condition != null)
				{
					// Existe una condicion especifica para este recurso
					L.debug("Existe una condicion de localicacion para el recurso [{}]", location_condition.get_location());

					String current_location = resource.get_running_node();

					if (current_location != null)
					{
						String conditioned_location = location_condition.get_location();
						int value_in_error = location_condition.get_on_mismatch();

						if (conditioned_location != null)
						{
							L.debug("La localizacion del recurso debe cumplir lo siguiente [{}]", conditioned_location);
							String real_conditioned_location = get_referenced_location(conditioned_location, results);

							if (real_conditioned_location != null)
							{
								if (current_location.equals(real_conditioned_location))
								{
									L.debug("Las localizaciones actual [{}] y referenciada [{}] coinciden. La referencia esta negada ? [{}]", current_location, real_conditioned_location, is_reference_negated(conditioned_location));
									sensor.addChannel(build_channel(resource.get_name(), (is_reference_negated(conditioned_location) ? value_in_error : ClusterwareResourceResult.STATUS_OK)));
								}
								else
								{
									L.debug("Las localizaciones actual [{}] y referenciada [{}] NO coinciden. La referencia esta negada ? [{}]", current_location, real_conditioned_location, is_reference_negated(conditioned_location));
									sensor.addChannel(build_channel(resource.get_name(), (is_reference_negated(conditioned_location) ? ClusterwareResourceResult.STATUS_OK : value_in_error)));
								}
							}
							else
							{
								L.debug("El recurso referenciado no esta disponible, solo comprobamos que esta ONLINE, pero mostramos warning");
								sensor.addChannel(build_channel(resource.get_name(), (resource.is_fully_running() ? ClusterwareResourceResult.STATUS_WARN : value_in_error)));
							}
						}
						else
						{
							L.debug("No hay condicion especifica de localizacion, solo comprobamos que esta ONLINE");
							sensor.addChannel(build_channel(resource.get_name(), (resource.is_fully_running() ? ClusterwareResourceResult.STATUS_OK : ClusterwareResourceResult.STATUS_ERROR)));
						}

					}
					else
					{
						L.debug("El recurso no esta corriendo en ningun nodo");
						sensor.addChannel(build_channel(resource.get_name(), ClusterwareResourceResult.STATUS_ERROR));
					}

				}
				else if (ogrid_res.is_type_mandatory(resource.get_type()))
				{
					// El recurso es de un tipo incluido en los mandatory types
					L.debug("El recurso [{}] es de tipo [{}], por lo que debe esar ONLINE en todos sus nodos.", resource.get_name(), resource.get_type());
					sensor.addChannel(build_channel(resource.get_name(), (resource.is_fully_running() ? ClusterwareResourceResult.STATUS_OK : ClusterwareResourceResult.STATUS_ERROR)));
				}
			}
			t.response.send(sensor, 200);
			return;
		}

		PrtgSensor error_sensor = new PrtgSensor();
		error_sensor.addChannel(new PrtgErrorResult("No hay datos de los recursos del GRID disponibles"));
		t.response.send(error_sensor, 200);
	}

	private String get_referenced_location(String src, Map<String, ClusterwareResourceResult> resouce_map)
	{
		L.debug("Calculamos donde hace referencia la configuracion del nodo [{}]", src);
		String referenced_resource_name = null;

		if (src.startsWith("RES "))
		{
			referenced_resource_name = src.substring(4, src.length());
		}
		else if (src.startsWith("!RES "))
		{
			referenced_resource_name = src.substring(5, src.length());
		}
		else
		{
			L.debug("La condicion hace referencia directamente al nodo [{}]", src);
			return src;
		}

		L.debug("La condicion de localizacion hace referencia al recurso [{}]", referenced_resource_name);
		ClusterwareResourceResult referenced_resource = resouce_map.get(referenced_resource_name);
		if (referenced_resource != null)
		{
			L.debug("El recurso referido se encuentra ejecutandose en el nodo [{}] (null = no esta en ejecucion)", referenced_resource.get_running_node());
			return referenced_resource.get_running_node();
		}
		else
		{
			L.debug("La condicion hace referencia a otro recurso [{}] que no hemos podido hayar", referenced_resource_name);
			return null;
		}
	}

	private boolean is_reference_negated(String src)
	{
		return (src.charAt(0) == '!');
	}

	private PrtgChannelResult build_channel(String resource_name, int value)
	{
		L.debug("Creando canal para el recurso [{}] con valor [{}]", resource_name, value);
		PrtgChannelResult channel = new PrtgChannelResult(resource_name, value, "Custom");
		channel.setValueLookup("prtg.standardlookups.cisco.ciscoenvmonstate");
		return channel;
	}

}
