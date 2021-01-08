package es.hefame.hagent.command.oracle.clusterwareresources.result;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleGridResourcesConfigData;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class ClusterwareResourceResult extends PrtgSensor
{
	private static Logger			L							= LogManager.getLogger();
	private static final Marker		OGRID_RESOUCE_CMD_MARKER	= MarkerManager.getMarker("OGRID_RESOURCE_CMD");

	public static final int			STATUS_OK					= 1;
	public static final int			STATUS_ERROR				= 3;
	public static final int			STATUS_WARN					= 2;
	public static final int			STATUS_UNKNOWN				= 5;

	private String					name						= null;
	private String					type						= null;
	private Map<String, Boolean>	state						= null;

	public ClusterwareResourceResult(String resource_raw)
	{

		String[] lines = resource_raw.split("\\n");

		if (lines.length != 4)
		{
			L.error(OGRID_RESOUCE_CMD_MARKER, "La cadena de entrada [{}] no tiene 4 lineas.", resource_raw);
			this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
			return;
		}

		String[] chunks;

		// NAME
		for (int i = 0; i < lines.length; i++)
		{
			chunks = lines[i].split("\\=");
			if (chunks.length != 2)
			{
				L.error(OGRID_RESOUCE_CMD_MARKER, "Al dividir la cadena [{}] por el simbolo '=', no salieron 2 resultados", lines[i]);
				this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
				return;
			}

			switch (chunks[0])
			{
				case "NAME":
				{
					if (this.name != null)
					{
						L.error(OGRID_RESOUCE_CMD_MARKER, "Aparece 2 veces la linea con la etiqueta '{}'. Linea [{}], valor previo [{}]", chunks[0], lines[i], this.name);
						this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
						return;
					}
					this.name = chunks[1];
					break;
				}
				case "TYPE":
				{
					if (this.type != null)
					{
						L.error(OGRID_RESOUCE_CMD_MARKER, "Aparece 2 veces la linea con la etiqueta '{}'. Linea [{}], valor previo [{}]", chunks[0], lines[i], this.type);
						this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
						return;
					}
					this.type = chunks[1];
					break;
				}
				case "STATE":
				{
					if (this.state != null)
					{
						L.error(OGRID_RESOUCE_CMD_MARKER, "Aparece 2 veces la linea con la etiqueta '{}'. Linea [{}], valor previo [{}]", chunks[0], lines[i], this.state);
						this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
						return;
					}

					String[] nodes = chunks[1].split("\\s*(\\,)\\s*");
					if (nodes.length == 0)
					{
						L.error(OGRID_RESOUCE_CMD_MARKER, "No parece posible, pero no hay nodos que explotar en la linea de STATE [{}]", chunks[1]);
						this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
						return;
					}

					this.state = new HashMap<String, Boolean>();
					int j = 0;
					for (String node : nodes)
					{
						j++;
						if (node.equals("OFFLINE"))
						{
							this.state.put("nodo" + j, false);
							continue;
						}

						String[] node_data = node.split("\\s*(on)\\s*");
						if (node_data.length != 2)
						{
							L.error(OGRID_RESOUCE_CMD_MARKER, "La cadena de estado de nodo [{}] no se partio en 2 al hacerle split '\\s*(on)\\s*'", node);
							this.addChannel(new PrtgErrorResult("No se pudo obtener el recurso del cluster"));
							return;
						}

						// this.state.put(node_data[1], "ONLINE".equalsIgnoreCase(node_data[0]));
						this.state.put(node_data[1], true);
					}
					break;
				}
				case "TARGET":
					break;

			}
		}
		this.channelize();

	}

	public String get_name()
	{
		return name;
	}

	public String get_type()
	{
		return type;
	}

	public boolean is_fully_running()
	{
		if (state.size() > 0)
		{
			for (Boolean b : state.values())
			{
				if (b.booleanValue() == false) return false;
			}
			return true;
		}
		return false;
	}

	public boolean is_running_in(String node)
	{
		Boolean b = state.get(node);
		if (b != null) return b.booleanValue();
		return false;
	}

	public String get_running_node()
	{
		for (Entry<String, Boolean> node : this.state.entrySet())
		{
			if (node.getValue() == true) { return node.getKey(); }
		}
		return null;
	}

	protected void channelize()
	{
		OracleGridResourcesConfigData ogrid_res = (OracleGridResourcesConfigData) CONF.checker.getMonitorizedElementByName("oracle_grid_resources");

		if (ogrid_res.is_type_mandatory(this.type))
		{
			L.debug(OGRID_RESOUCE_CMD_MARKER, "El recurso [{}] es de tipo [{}], por lo que debe esar ONLINE en todos sus nodos", this.get_name(), this.get_type());
			PrtgChannelResult channel = new PrtgChannelResult("Recurso " + name, (this.is_fully_running() ? 1 : 0), "Custom");
			channel.setValueLookup("prtg.standardlookups.yesno.stateyesok");
			this.addChannel(channel);
		}

	}

}
