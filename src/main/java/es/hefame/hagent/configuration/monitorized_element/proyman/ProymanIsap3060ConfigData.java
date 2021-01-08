package es.hefame.hagent.configuration.monitorized_element.proyman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;

public class ProymanIsap3060ConfigData extends MonitorizedElementConfigData
{
	private static Logger		L			= LogManager.getLogger();
	private static final int	BUFFER_SIZE	= 1000;

	private String				file;
	private int					bufferSize;
	private List<String>		werks;

	public ProymanIsap3060ConfigData(JSONObject jsonRoot) throws HException
	{
		if (jsonRoot == null) throw new HException("El elemento de configuracion es nulo");
		L.debug("Parseando informacion del objeto PROYMAN ISAP3060");

		this.type = "proyman_isap3060";
		this.name = "proyman_isap3060";

		String elementName;
		Object elementObj;

		elementName = "file";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			file = elementObj.toString();
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		elementName = "buffer_size";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			try
			{
				bufferSize = Integer.valueOf(elementObj.toString());
			}
			catch (NumberFormatException e)
			{
				L.warn("No se pudo convertir el valor del campo '{}' a un entero. Se usa el valor por defecto {}", elementName, BUFFER_SIZE);
				L.catching(e);
				bufferSize = BUFFER_SIZE;
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

		elementName = "werks";
		elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			if (elementObj instanceof JSONArray)
			{
				JSONArray jsonWerksArray = (JSONArray) elementObj;

				this.werks = new ArrayList<String>(jsonWerksArray.size());

				@SuppressWarnings("unchecked")
				Iterator<Object> it = jsonWerksArray.iterator();
				while (it.hasNext())
				{
					Object o = it.next();
					if (o != null)
					{
						String werk = o.toString();
						if (werk.matches("RG[0-9]{2}"))
						{
							this.werks.add(o.toString());
						}
						else
						{
							L.warn("Se ignora el elemento [{}] encontrado en el array 'werks' por no pasar el patron", werk);
						}
					}
					else
					{
						L.warn("Se ignora un elemento nulo encontrado en el array 'werks'");
					}
				}

			}
			else
			{
				L.debug("El valor de '{}' es obligatorio que sea un array json", elementName);
				throw new HException("El parametro [" + elementName + "] no es un array json");
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}'. Este es obligatorio para el tipo de objeto", elementName);
			throw new HException("No se encuentra el parametro [" + elementName + "]");
		}

	}

	public String getFile()
	{
		return file;
	}

	public int getBufferSize()
	{
		return bufferSize;
	}

	public String[] getWerks()
	{
		return werks.toArray(new String[0]);
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("name", this.name);
		root.put("file", this.file);
		root.put("buffer_size", this.bufferSize);

		JSONArray werksArray = new JSONArray();
		werksArray.addAll(this.werks);
		root.put("werks", werksArray);

		return root;
	}

}
