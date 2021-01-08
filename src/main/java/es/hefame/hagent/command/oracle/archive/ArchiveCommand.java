package es.hefame.hagent.command.oracle.archive;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hcore.converter.StringConverter;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.ArchivelogConfigData;

public abstract class ArchiveCommand implements Command
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	ARCHIVE_CMD_MARKER	= MarkerManager.getMarker("ARCHIVE_CMD");

	public static ArchiveCommand instanciate(ArchivelogConfigData config) throws HException
	{
		String class_name = ArchiveCommand.class.getPackage().getName() + "." + StringConverter.upperCaseFirst(config.get_subtype()) + "ArchiveCommand";
		L.debug(ARCHIVE_CMD_MARKER, "Instanciando la clase [{}]", class_name);
		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends ArchiveCommand> clazz = (Class<? extends ArchiveCommand>) Class.forName(class_name);
			Constructor<? extends ArchiveCommand> c = clazz.getConstructor(ArchivelogConfigData.class);
			return c.newInstance(config);
		}
		catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			L.error(ARCHIVE_CMD_MARKER, "Ocurrio una excepcion al instanciar el comando con la configuracion [{}]", config);
			L.catching(e);
			throw new HException("Error al instanciar el comando de Archivelog", e);
		}

	}

	public abstract OsCommandResult operate() throws HException;
}
