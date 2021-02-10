package es.hefame.hagent.command.oracle.archive;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.filesystems.result.FilesystemResult;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.ArchivelogConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.DeleteArchiveConfigData;

public class DeleteArchiveCommand extends ArchiveCommand {
	private static Logger L = LogManager.getLogger();
	private static final Marker ARCHIVE_CMD_MARKER = MarkerManager.getMarker("ARCHIVE_CMD");

	private DeleteArchiveConfigData config = null;

	public DeleteArchiveCommand(ArchivelogConfigData configData) throws HException {
		if (configData instanceof DeleteArchiveConfigData) {
			this.config = (DeleteArchiveConfigData) configData;
		} else {
			L.error(ARCHIVE_CMD_MARKER,	"No se puede crear el comando [{}] porque la configuracion es incompatible [{}]", this.getClass().getSimpleName(), configData.getClass().getSimpleName());
			throw new HException("Comando incompatible");
		}
	}

	@Override
	public OsCommandResult operate() throws HException {

		if (this.config.getArchivePercentOk() == 0) {
			String stdInput = "delete noprompt archivelog all;";

			try {
				OsCommandExecutor comm = new OsCommandExecutor(ARCHIVE_CMD_MARKER, "su", "-", config.getUser(), "-c", "rman target /");
				OsCommandResult result = comm.run(stdInput.getBytes());
				L.debug(ARCHIVE_CMD_MARKER, "El resultado de la operacion es: [{}]", () -> result.toString());
				return result;
			} catch (IOException e) {
				L.error(ARCHIVE_CMD_MARKER, "Ocurrio una excepcion al ejecutar el comando");
				L.catching(e);
				throw new HException("Ocurrio una excepcion al ejecutar el comando", e);
			}
		} else {

			String[] untils = { 
					"SYSDATE-2", 
					"SYSDATE-(36/24)", 
					"SYSDATE-1", 
					"SYSDATE-(18/24)", 
					"SYSDATE-(12/24)", 
					"SYSDATE-(6/24)", 
					"SYSDATE-(3/24)",
					"SYSDATE-(2/24)",
					"SYSDATE-(1/24)"
			};
			
			OsCommandResult result = null;
			
			for (int i = 0 ; i <= untils.length ; i++) {

				String untilTime = " all";
				// Notese que la �ltima iteracion i == untils.length, por lo que until_time acabar�a valiendo ""
				if (i < untils.length) {
					untilTime = " until time \"" + untils[i] + "\"";
				}

				String stdInput = "delete noprompt archivelog " + untilTime + ";";				
				L.info("Se procede al borrado con el siguiente comando RMAN [{}]", stdInput);

				try {
					OsCommandExecutor comm = new OsCommandExecutor(ARCHIVE_CMD_MARKER, "su", "-", config.getUser(), "-c", "rman target /");
					result = comm.run(stdInput.getBytes());
					L.debug(ARCHIVE_CMD_MARKER, "El resultado de la operacion es: [{}]", result.toString());
				} catch (IOException e) {
					L.error(ARCHIVE_CMD_MARKER, "Ocurrio una excepcion al ejecutar el comando");
					L.catching(e);
					throw new HException("Ocurrio una excepcion al ejecutar el comando", e);
				}

				if (i == untils.length) {
					// Si ya hemos intentado borrar archivelogs con todos los rangos del mundo, no hace falta que esperemos al sampler,
					// ya que el checker lo har� por nosotros.
					L.debug("Esta era la �ltima operacion de borrado, el comando retorna y el checker que lo llamo decidira que hacer");
					return result;
				}
				
				
				try {
					L.debug("Vamos a esperar que los Samplers de filesystems se refresquen antes de comprobar si debemos hacer un borrado mas agresivo. [{} milisecs]", BgJobs.FILESYSTEM_SAMPLERS_DELAY);
					Thread.sleep(BgJobs.FILESYSTEM_SAMPLERS_DELAY);
				} catch (InterruptedException e) {
					// Si nos interrumpen, abortamos pero no limpiamos el flag de interrupcion para que nuestro padre lo sepa
					L.catching(e);
					throw new HException("La ejecucion del comando fue interrumpida", e);
				}
				
				FilesystemResult newArchiveDestResult = getFsSensor(config.getArchiveDest());
				L.debug("La nueva ocupacion del FS es [{}%]", newArchiveDestResult.get_used_bytes_percentage());
				if (newArchiveDestResult.get_used_bytes_percentage() < config.getArchivePercentOk()) {
					L.debug("La nueva ocupacion del FS es [{}%] y est� por debajo del umbral del [{}%]", newArchiveDestResult.get_used_bytes_percentage(), newArchiveDestResult.get_used_bytes_percentage());
					return result;
				} else {
					L.info("La nueva ocupacion del FS es [{}%] y est� por encima del umbral del [{}%]", newArchiveDestResult.get_used_bytes_percentage(), newArchiveDestResult.get_used_bytes_percentage());
					L.info("Se procede al borrado con un rango de tiempo menos restrictivo");
				}
			}
			
			return result;

		}

	}
	
	
	private FilesystemResult getFsSensor(String mountPoint) throws HException
	{
		Sampler s = null;
		String samplerName;
		if (mountPoint.charAt(0) == '+')
		{
			samplerName = "asm_diskgroups";
		}
		else
		{
			samplerName = "filesystems";
		}

		s = (Sampler) BgJobs.getJob(samplerName);

		if (s != null)
		{
			@SuppressWarnings("unchecked")
			Map<String, FilesystemResult> results = (Map<String, FilesystemResult>) s.getLastResult();
			if (results != null)
			{
				FilesystemResult archiveDestResult = results.get(mountPoint);
				if (archiveDestResult != null)
				{
					return archiveDestResult;
				}
				else
				{
					// No hay datos del FS especificado en config.get_archive_dest()
					L.error("No hay resultados para el filesystem [{}] en el sampler [{}]", mountPoint, samplerName);
					throw new HException("No hay resultados para el filesystem [" + mountPoint + "] en el sampler [" + samplerName + "]");
				}
			}
			else
			{
				// No hay resultados del Sampler
				L.error("No hay resultados en el sampler de filesystems");
				throw new HException("No hay resultados en el sampler de filesystems");
			}
		}
		else
		{
			// No hay sampler de FS
			L.error("El sampler [{}] no se encuentra operativo. No se pueden obtener datos de los filesystems", samplerName);
			throw new HException("El sampler de filesystems no se encuentra disponible");
		}

	}

}


