package es.hefame.hagent.bg.checker.oracle;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.converter.DateConverter;
import es.hefame.hcore.HException;
import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.bg.checker.Checker;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.filesystems.result.FilesystemResult;
import es.hefame.hagent.command.oracle.archive.ArchiveCommand;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.alert_channel.AlertChannel;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.ArchivelogConfigData;
import es.hefame.hagent.util.exception.CommandException;
import es.hefame.hagent.util.mail.html.HtmlHeader;
import es.hefame.hagent.util.mail.html.HtmlParagraph;
import es.hefame.hagent.util.mail.html.HtmlStyler;

public class OracleArchivelogChecker extends Checker
{
	private static Logger		L		= LogManager.getLogger();

	

	public OracleArchivelogChecker(String dbName)
	{
		super("archivelog_" + dbName.toLowerCase(), BgJobs.FILESYSTEM_SAMPLERS_DELAY);
		this.delayIfException = 600000; // Si el archivado falla, esperamos al menos 10 minutos antes de proceder con el siguiente intento
		
	}

	@Override
	public void operate() throws HException
	{
		ArchivelogConfigData config = (ArchivelogConfigData) CONF.checker.getMonitorizedElementByName(this.getCheckerName());
		if (config == null) { throw new HException("La configuracion del elemento [" + this.getCheckerName() + "] no se encuentra disponible"); }

		FilesystemResult archiveDestResult = getFsSensor(config.getArchiveDest());

		L.debug("Filesystem [{}] ocupado al [{}%]. Umbral de archivado en [{}%]", config.getArchiveDest(), archiveDestResult.get_used_bytes_percentage(), config.getArchivePercent());
		if (archiveDestResult.get_used_bytes_percentage() >= config.getArchivePercent())
		{
			// Debemos lanzar el archivado
			L.info("El filesystem [{}] esta ocupado por encima del umbral [{}% > {}%]. Lanzamos arhivado de tipo [{}]", config.getArchiveDest(), archiveDestResult.get_used_bytes_percentage(), config.getArchivePercent(), config.getSubtype());

			long starttime = System.currentTimeMillis();
			OsCommandResult result = ArchiveCommand.instanciate(config).operate();
			long elapsedArchiveTime = (System.currentTimeMillis() - starttime);

			try
			{
				L.debug("Vamos a esperar que los Samplers de filesystems se refresquen antes de comprobar si el archivado ha ido bien. [{} milisecs]", BgJobs.FILESYSTEM_SAMPLERS_DELAY);
				Thread.sleep(BgJobs.FILESYSTEM_SAMPLERS_DELAY);
			}
			catch (InterruptedException e) // NOTA: No limpiar el flag interrupted, el padre necesita saber si ha sido interrumpido 
			{
				// Si nos interrumpen, abortamos per no limpiamos el flag de interrupcion para que nuestro padre lo sepa
				L.catching(e);
				return;
			}

			FilesystemResult newArchiveDestResult = getFsSensor(config.getArchiveDest());
			if (newArchiveDestResult.get_used_bytes_percentage() >= config.getArchivePercent())
			{
				// La cosa pinta mal, debemos alertar !
				L.error("Error en el archivado. El filesystem [{}] sigue por encima del umbral [{}% > {}%].", config.getArchiveDest(), newArchiveDestResult.get_used_bytes_percentage(), config.getArchivePercent());

				if (this.alertsEnabled())
				{
					StringBuilder message = new StringBuilder();
					message.append(new HtmlStyler());
					message.append(new HtmlHeader("Error al archivar la base de datos " + config.getDbName(), 1, "error"));
					message.append(new HtmlParagraph("Tras ejecutar el archivado, el filesystem [" + config.getArchiveDest() + "] est&aacute; ocupado al " + newArchiveDestResult.get_used_bytes_percentage() + "%, encima del umbral permitido del " + config.getArchivePercent() + "%."));
					message.append(new HtmlParagraph("Antes de ejecutar el archivado, el filesystem estaba ocupado al " + archiveDestResult.get_used_bytes_percentage() + "%."));
					message.append(new HtmlParagraph("El comando de archivado ha tardado " + DateConverter.milisecToHuman(elapsedArchiveTime)));
					message.append(new HtmlParagraph("<pre class='code'>" + result.toString() + "</pre>", "code"));
					message.append(this.getAlertLinks());

					AlertChannel channel = CONF.channels.get_channel(config.getAlertChannel());
					channel.send("Error al archivar la base de datos " + config.getDbName(), message.toString());
				}

				// Lanzamos la excepcion para que no se cuente como exito.
				throw new CommandException(result, "El archivado fallo");

			}
			else
			{
				L.info("Archivado completado con exito. El filesystem [{}] esta ocupado por debajo del umbral [{}% > {}%].", newArchiveDestResult.get_mount_point(), newArchiveDestResult.get_used_bytes_percentage(), config.getArchivePercent());
				if (this.alertsEnabled())
				{
					AlertChannel channel = CONF.channels.get_channel_if_exists("success");
					if (channel != null)
					{
						StringBuilder message = new StringBuilder();
						message.append(new HtmlStyler());
						message.append(new HtmlHeader("Base de datos archivada con &eacute;xito " + config.getDbName(), 1, "ok"));
						message.append(new HtmlParagraph("Tras ejecutar el archivado, el filesystem [" + config.getArchiveDest() + "] est&aacute; ocupado al " + newArchiveDestResult.get_used_bytes_percentage() + "%."));
						message.append(new HtmlParagraph("Antes de ejecutar el archivado, el filesystem estaba ocupado al " + archiveDestResult.get_used_bytes_percentage() + "%."));
						message.append(new HtmlParagraph("El comando de archivado ha tardado " + DateConverter.milisecToHuman(elapsedArchiveTime)));
						message.append(new HtmlParagraph("<pre class='code'>" + result.toString() + "</pre>", "code"));
						message.append(this.getAlertLinks());
						channel.send("Exito al archivar la base de datos " + config.getDbName(), message.toString());
					}
				}
			}
		}
		else
		{
			// Nada que hacer
			L.debug("El filesystem esta por debajo del umbral de utilizacion. No hacemos nada.");
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
