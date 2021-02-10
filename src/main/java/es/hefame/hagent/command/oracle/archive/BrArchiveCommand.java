package es.hefame.hagent.command.oracle.archive;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.ArchivelogConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.BrArchiveConfigData;

public class BrArchiveCommand extends ArchiveCommand
{
	private static Logger		L					= LogManager.getLogger();
	private static final Marker	ARCHIVE_CMD_MARKER	= MarkerManager.getMarker("ARCHIVE_CMD");

	private BrArchiveConfigData	config				= null;

	public BrArchiveCommand(ArchivelogConfigData configData) throws HException
	{
		if (configData instanceof BrArchiveConfigData)
		{
			this.config = (BrArchiveConfigData) configData;
		}
		else
		{
			L.error(ARCHIVE_CMD_MARKER, "No se puede crear el comando [{}] porque la configuracion es incompatible [{}]", this.getClass().getSimpleName(), configData.getClass().getSimpleName());
			throw new HException("Comando incompatible");
		}
	}

	@Override
	public OsCommandResult operate() throws HException
	{
		try
		{
			String brcommand = "brarchive -u " + config.getBrUser() + " -c force -p " + config.getSapFile() + " " + config.getBrOption();
			OsCommandExecutor comm = new OsCommandExecutor(ARCHIVE_CMD_MARKER, "su", "-", config.getUser(), "-c", brcommand);
			OsCommandResult result = comm.run();
			L.debug(ARCHIVE_CMD_MARKER, "El resultado de la operacion es: [{}]", () -> result.toString());
			return result;
		}
		catch (IOException e)
		{
			L.error(ARCHIVE_CMD_MARKER, "Ocurrio una excepcion al ejecutar el comando");
			L.catching(e);
			throw new HException("Ocurrio una excepcion al ejecutar el comando", e);
		}

	}

}
