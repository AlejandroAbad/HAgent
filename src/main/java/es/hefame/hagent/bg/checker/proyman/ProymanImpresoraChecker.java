package es.hefame.hagent.bg.checker.proyman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.configuration.monitorized_element.proyman.ProymanImpresoraConfigData;
import es.hefame.hagent.util.EvictingList;
import es.hefame.hagent.util.exception.CommandException;

public class ProymanImpresoraChecker extends Checker
{
	private static Logger				L					= LogManager.getLogger();

	private long						last_file_length	= 0;
	private FileReader					file_reader			= null;
	private BufferedReader				buffer				= null;
	private ProymanImpresoraConfigData	config				= null;

	private EvictingList<ImpresoraLine>	lines				= new EvictingList<ImpresoraLine>(1000);

	public ProymanImpresoraChecker(ProymanImpresoraConfigData config)
	{
		super("proyman_impresora", 1000);
		this.config = config;
	}

	private void open_file_and_read_config() throws HException
	{
		File file = new File(config.get_file());
		long current_size = file.length();

		if (current_size < this.last_file_length)
		{
			L.info("El fichero se ha reducido. Forzamos la reapurtura del mismo.");
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

		// TODO: Permitir cambiar la ruta del fichero al vuelo ?? (!current_file.equals(config.get_file()))
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

		this.open_file_and_read_config();

		String line = null;
		try
		{
			while ((line = buffer.readLine()) != null)
			{
				L.trace("Leida linea [{}]", line);

				try
				{
					ImpresoraLine l = new ImpresoraLine(line);
					lines.add(l);
				}
				catch (CommandException e)
				{
					L.error("Se ignora la linea [{}] por causar una excepcion", line);
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

	public ImpresoraLine[] get_lines()
	{
		return this.lines.toArray(new ImpresoraLine[0]);
	}

	public class ImpresoraLine implements JsonEncodable
	{
		public final int			no_lines;
		public final float			process_time;
		public final ImpresoraTipo	type;
		public final String			server;
		public final long			timestamp	= System.currentTimeMillis();

		/*
		 * 10025|0020827594|0000027594|20170627|115751|001| 000|135C1A25|-Chequeo--|proccli10813812_01| |00000.07|20170627|115751|sap2p01_P|RG19
		 * 10028|0010117369|0000017369|20170629|082237|024| 033|63493B4E|2007606720|proccli4653410_01| |00002.57|20170629|082238|sap2p01_P|RG01
		 * 10025|0010117867|0000017867|20170629|082747|001| 000|B4B75577|ErrPedido-|proccli61210668_01|No se ha indicado so|00000.02|20170629|082747|sap6p01_P|
		 * | | | | |000| | |Desconexio|proccli12452212_01| |00000.00|20170629|071519
		 * | | | | |000| | |-Conexion-|proccli49283136_01| |00000.01|20170629|071731| |
		 */

		public ImpresoraLine(String line) throws CommandException
		{
			String[] chunks = line.split("\\|");
			if (chunks.length < 14) { throw new CommandException("La linea de impresora no tiene 14 pedazos separados por '|'"); }

			try
			{
				no_lines = Integer.parseInt(chunks[5]);
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a entero el numero de lineas de pedido");
				L.catching(e);
				throw new CommandException("No se puede convertir a entero el numero de lineas de pedido", e);
			}

			try
			{
				process_time = Float.parseFloat(chunks[11]);
			}
			catch (NumberFormatException e)
			{
				L.error("No se puede convertir a float el tiempo que ha tardado la linea");
				L.catching(e);
				throw new CommandException("No se puede convertir a entero el tiempo que ha tardado la linea", e);
			}

			switch (chunks[8])
			{
				case "-Chequeo--":
					this.type = ImpresoraTipo.CHECKEO;
					break;
				case "ErrPedido-":
					this.type = ImpresoraTipo.ERRPEDIDO;
					break;
				case "Desconexio":
				case "-Conexion-":
					this.type = ImpresoraTipo.OTRO;
					break;
				default:
					this.type = ImpresoraTipo.PEDIDO;
					break;
			}

			this.server = (chunks.length > 14 && chunks[14] != null) ? chunks[14].toLowerCase() : "";

		}

		@SuppressWarnings("unchecked")
		@Override
		public JSONAware jsonEncode()
		{
			JSONObject json = new JSONObject();
			json.put("lineas", no_lines);
			json.put("time", process_time);
			json.put("tipo", type.name());
			json.put("servidor", server);
			json.put("timestamp", timestamp);
			return json;
		}

	}

	public enum ImpresoraTipo
	{
		CHECKEO, PEDIDO, ERRPEDIDO, OTRO
	}

}
