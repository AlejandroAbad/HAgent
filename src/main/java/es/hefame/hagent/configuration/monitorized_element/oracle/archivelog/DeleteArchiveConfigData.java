package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;

public class DeleteArchiveConfigData extends ArchivelogConfigData
{
	private static Logger		L				= LogManager.getLogger();
	
	protected int				archive_percent_ok;
	
	
	// private static Logger L = LogManager.getLogger();
	public DeleteArchiveConfigData(JSONObject json_root) throws HException
	{
		super(json_root, "delete");

		
		// ARCHIVE PERCENT (obligatorio y en el rango de 0 a 100)
		String element_name = "archive_percent_ok";
		Object element_obj = json_root.get(element_name);
		if (element_obj != null)
		{
			try
			{
				archive_percent_ok = Integer.parseInt(element_obj.toString());

				if (archive_percent_ok > this.get_archive_percent()) {
					L.warn("El valor de '{}' [{}] es inferior al de 'archive_percent', por lo que no tendr� efecto y se asume 0.", element_name, archive_percent);
					archive_percent_ok = 0;
				}
				if (archive_percent_ok > 100 || archive_percent_ok < 0)
				{
					L.debug("El valor de '{}' es [{}]. Debe encontrarse entre 0 y 100", element_name, archive_percent);
					throw new HException("No se encuentra el parametro [" + element_name + "]");
				}
			}
			catch (NumberFormatException e)
			{
				L.catching(e);
				L.warn("El valor de '{}' no es un entero valido. Se asume el valor 0", element_name);
				archive_percent_ok = 0;
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}', se asume 0.", element_name);
			archive_percent_ok = 0;
		}
		
	}
	
	public int get_archive_percent_ok() {
		return this.archive_percent_ok;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("archive_percent_ok", this.archive_percent_ok);
		return root;
	}

}
