package es.hefame.hagent.configuration.monitorized_element.oracle.archivelog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;

public class DeleteArchiveConfigData extends ArchivelogConfigData
{
	private static Logger		L				= LogManager.getLogger();
	
	protected int				archivePercentOk;
	
	
	// private static Logger L = LogManager.getLogger();
	public DeleteArchiveConfigData(JSONObject jsonRoot) throws HException
	{
		super(jsonRoot, "delete");

		
		// ARCHIVE PERCENT (obligatorio y en el rango de 0 a 100)
		String elementName = "archive_percent_ok";
		Object elementObj = jsonRoot.get(elementName);
		if (elementObj != null)
		{
			try
			{
				archivePercentOk = Integer.parseInt(elementObj.toString());

				if (archivePercentOk > this.getArchivePercent()) {
					L.warn("El valor de '{}' [{}] es inferior al de 'archive_percent', por lo que no tendr� efecto y se asume 0.", elementName, archivePercent);
					archivePercentOk = 0;
				}
				if (archivePercentOk > 100 || archivePercentOk < 0)
				{
					L.debug("El valor de '{}' es [{}]. Debe encontrarse entre 0 y 100", elementName, archivePercent);
					throw new HException("No se encuentra el parametro [" + elementName + "]");
				}
			}
			catch (NumberFormatException e)
			{
				L.catching(e);
				L.warn("El valor de '{}' no es un entero valido. Se asume el valor 0", elementName);
				archivePercentOk = 0;
			}
		}
		else
		{
			L.debug("No se haya el valor de '{}', se asume 0.", elementName);
			archivePercentOk = 0;
		}
		
	}
	
	public int getArchivePercentOk() {
		return this.archivePercentOk;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = super.jsonEncode();
		root.put("archive_percent_ok", this.archivePercentOk);
		return root;
	}

}
