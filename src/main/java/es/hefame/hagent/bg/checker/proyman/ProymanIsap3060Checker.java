package es.hefame.hagent.bg.checker.proyman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.configuration.monitorized_element.proyman.ProymanIsap3060ConfigData;
import es.hefame.hagent.util.EvictingList;
import es.hefame.hagent.util.exception.CommandException;

public class ProymanIsap3060Checker extends Checker
{
	private static Logger							L					= LogManager.getLogger();
	private static final Marker						M					= MarkerManager.getMarker("ISAP3060");

	private final String							FILTER				= "ISAM20 Albaran";

	private long									last_file_length	= 0;
	private FileReader								file_reader			= null;
	private BufferedReader							buffer				= null;

	private String									fileName;

	private Map<String, EvictingList<Isap3060Line>>	buffers				= new HashMap<String, EvictingList<Isap3060Line>>();

	public ProymanIsap3060Checker(ProymanIsap3060ConfigData config)
	{
		super("proyman_isap3060", 1000);
		this.fileName = config.getFile();
		int bufferSize = config.getBufferSize();

		for (String werk : config.getWerks())
		{
			buffers.put(werk, new EvictingList<Isap3060Line>(bufferSize));
			L.debug(M, "Creado buffer de tamano [{}] para el centro [{}]", bufferSize, werk);
		}

	}

	private void readConfigAndOpenFile() throws HException
	{
		File file = new File(fileName);
		long current_size = file.length();

		if (current_size < this.last_file_length)
		{
			L.info(M, "El fichero se ha reducido. Forzamos la reapurtura del mismo.");
			if (this.file_reader != null)
			{
				try
				{
					this.file_reader.close();
				}
				catch (IOException e)
				{
				}
				this.file_reader = null;
			}
			if (this.buffer != null)
			{
				try
				{
					this.buffer.close();
				}
				catch (IOException e)
				{
				}
				this.buffer = null;
			}
		}
		this.last_file_length = current_size;

		// TODO: Permitir cambiar la ruta del fichero al vuelo ?? if (!current_file.equals(config.get_file()))
		if (this.file_reader == null || this.buffer == null)
		{
			try
			{
				this.file_reader = new FileReader(file);
				buffer = new BufferedReader(this.file_reader);
				buffer.skip(file.length());
			}
			catch (IOException e)
			{
				try
				{
					if (this.file_reader != null) this.file_reader.close();
					if (this.buffer != null) this.buffer.close();
					this.file_reader = null;
					this.buffer = null;
				}
				catch (IOException e1)
				{
					L.catching(e1);
				}
				throw new HException("Error al abrir el fichero de log", e);
			}
		}
	}

	@Override
	public void operate() throws HException
	{
		this.readConfigAndOpenFile();

		String line = null;
		try
		{
			while ((line = buffer.readLine()) != null)
			{
				L.trace(M, "Leida linea [{}]", line);

				if (!line.contains(FILTER))
				{
					L.trace(M, "Se ignora por no pasar el filtro");
					continue;
				}

				try
				{
					Isap3060Line l = new Isap3060Line(line);

					EvictingList<Isap3060Line> list = buffers.get(l.centro);
					if (list != null)
					{
						list.add(l);
					}
					else
					{
						L.trace(M, "Se ignora la linea porque no existe el buffer para el centro [{}]", l.centro);
					}

				}
				catch (CommandException e)
				{
					L.error(M, "Se ignora la linea [{}] por causar una excepcion", line);
					L.catching(e);
				}

			}
		}
		catch (IOException e)
		{
			L.catching(e);
			throw new HException("Error leyendo el fichero de impresora", e);
		}
	}

	public List<Isap3060Line> getLinesForWerks(String werks)
	{
		if (!buffers.containsKey(werks)) return null;

		EvictingList<Isap3060Line> list = buffers.get(werks);
		List<Isap3060Line> l = new ArrayList<>(list);
		return l;
	}

	public class Isap3060Line implements JsonEncodable
	{
		private Date	horaLog;
		private String	pedido;
		private String	centro;
		private String	albaran;
		private int		hoja;

		/*
		 * 2 23/02 13:20:38 <0066912290>ISAM20 Albaran <RG10><0116170843><001><201802231220385763640><001>
		 */

		public Isap3060Line(String line) throws CommandException
		{
			try
			{

				int dd = Integer.parseInt(line.substring(2, 4));
				int mm = Integer.parseInt(line.substring(5, 7));
				int yy = (new GregorianCalendar()).get(Calendar.YEAR);

				int hh = Integer.parseInt(line.substring(8, 10));
				int ii = Integer.parseInt(line.substring(11, 13));
				int ss = Integer.parseInt(line.substring(14, 16));

				Calendar c = GregorianCalendar.getInstance();
				c.set(yy, mm, dd, hh, ii, ss);
				horaLog = c.getTime();

				pedido = line.substring(18, 28);
				centro = line.substring(45, 49);
				albaran = line.substring(51, 61);
				hoja = Integer.parseInt(line.substring(63, 66));

				if (L.isTraceEnabled(M))
				{
					L.debug(M, "Leida linea ISAP3060: [{}/{}/{} {}:{}:{}][{}][{}][{}][{}]", dd, mm, yy, hh, ii, ss, centro, pedido, albaran, hoja);
				}

			}
			catch (NumberFormatException nfe)
			{
				L.error(M, "Excepcion al interpretar la linea ISAP3060 [{}]");
				L.catching(nfe);
				throw new CommandException("Fallo en la interpretacion de la linea en ISAP3060");
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONAware jsonEncode()
		{
			JSONObject json = new JSONObject();
			json.put("timestamp", horaLog.getTime());
			json.put("pedido", pedido);
			json.put("centro", centro);
			json.put("albaran", albaran);
			json.put("hoja", hoja);
			return json;
		}

	}

}
