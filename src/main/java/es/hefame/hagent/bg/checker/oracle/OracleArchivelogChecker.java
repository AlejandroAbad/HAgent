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

	private ArchivelogConfigData	config	= null;

	public OracleArchivelogChecker(String db_name)
	{
		super("archivelog_" + db_name.toLowerCase(), BgJobs.FILESYSTEM_SAMPLERS_DELAY);
		this.delayIfException = 10 * 60 * 1000; // Si el archivado falla, esperamos al menos 10 minutos antes de proceder con el siguiente intento
		
	}

	@Override
	public void operate() throws HException
	{
		this.config = (ArchivelogConfigData) CONF.checker.getMonitorizedElementByName(this.getCheckerName());
		if (this.config == null) { throw new HException("La configuracion del elemento [" + this.getCheckerName() + "] no se encuentra disponible"); }

		FilesystemResult archive_dest_result = get_fs_sensor(config.get_archive_dest());

		L.debug("Filesystem [{}] ocupado al [{}%]. Umbral de archivado en [{}%]", config.get_archive_dest(), archive_dest_result.get_used_bytes_percentage(), config.get_archive_percent());
		if (archive_dest_result.get_used_bytes_percentage() >= config.get_archive_percent())
		{
			// Debemos lanzar el archivado
			L.info("El filesystem [{}] esta ocupado por encima del umbral [{}% > {}%]. Lanzamos arhivado de tipo [{}]", config.get_archive_dest(), archive_dest_result.get_used_bytes_percentage(), config.get_archive_percent(), config.get_subtype());

			long starttime = System.currentTimeMillis();
			OsCommandResult result = ArchiveCommand.instanciate(config).operate();
			long elapsed_archive_time = (System.currentTimeMillis() - starttime);

			try
			{
				L.debug("Vamos a esperar que los Samplers de filesystems se refresquen antes de comprobar si el archivado ha ido bien. [{} milisecs]", BgJobs.FILESYSTEM_SAMPLERS_DELAY);
				Thread.sleep(BgJobs.FILESYSTEM_SAMPLERS_DELAY);
			}
			catch (InterruptedException e)
			{
				// Si nos interrumpen, abortamos per no limpiamos el flag de interrupcion para que nuestro padre lo sepa
				L.catching(e);
				return;
			}

			FilesystemResult new_archive_dest_result = get_fs_sensor(config.get_archive_dest());
			if (new_archive_dest_result.get_used_bytes_percentage() >= config.get_archive_percent())
			{
				// La cosa pinta mal, debemos alertar !
				L.error("Error en el archivado. El filesystem [{}] sigue por encima del umbral [{}% > {}%].", config.get_archive_dest(), new_archive_dest_result.get_used_bytes_percentage(), config.get_archive_percent());

				if (this.alertsEnabled())
				{
					StringBuilder message = new StringBuilder();
					message.append(new HtmlStyler());
					message.append(new HtmlHeader("Error al archivar la base de datos " + config.get_db_name(), 1, "error"));
					message.append(new HtmlParagraph("Tras ejecutar el archivado, el filesystem [" + config.get_archive_dest() + "] est&aacute; ocupado al " + new_archive_dest_result.get_used_bytes_percentage() + "%, encima del umbral permitido del " + config.get_archive_percent() + "%."));
					message.append(new HtmlParagraph("Antes de ejecutar el archivado, el filesystem estaba ocupado al " + archive_dest_result.get_used_bytes_percentage() + "%."));
					message.append(new HtmlParagraph("El comando de archivado ha tardado " + DateConverter.milisecToHuman(elapsed_archive_time)));
					message.append(new HtmlParagraph("<pre class='code'>" + result.toString() + "</pre>", "code"));
					message.append(this.getAlertLinks());

					AlertChannel channel = CONF.channels.get_channel(config.get_alert_channel());
					channel.send("Error al archivar la base de datos " + config.get_db_name(), message.toString());
				}

				// Lanzamos la excepcion para que no se cuente como exito.
				throw new CommandException(result, "El archivado fallo");

			}
			else
			{
				L.info("Archivado completado con exito. El filesystem [{}] esta ocupado por debajo del umbral [{}% > {}%].", new_archive_dest_result.get_mount_point(), new_archive_dest_result.get_used_bytes_percentage(), config.get_archive_percent());
				if (this.alertsEnabled())
				{
					AlertChannel channel = CONF.channels.get_channel_if_exists("success");
					if (channel != null)
					{
						StringBuilder message = new StringBuilder();
						message.append(new HtmlStyler());
						message.append(new HtmlHeader("Base de datos archivada con &eacute;xito " + config.get_db_name(), 1, "ok"));
						message.append(new HtmlParagraph("Tras ejecutar el archivado, el filesystem [" + config.get_archive_dest() + "] est&aacute; ocupado al " + new_archive_dest_result.get_used_bytes_percentage() + "%."));
						message.append(new HtmlParagraph("Antes de ejecutar el archivado, el filesystem estaba ocupado al " + archive_dest_result.get_used_bytes_percentage() + "%."));
						message.append(new HtmlParagraph("El comando de archivado ha tardado " + DateConverter.milisecToHuman(elapsed_archive_time)));
						message.append(new HtmlParagraph("<pre class='code'>" + result.toString() + "</pre>", "code"));
						message.append(this.getAlertLinks());
						channel.send("Exito al archivar la base de datos " + config.get_db_name(), message.toString());
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

	private FilesystemResult get_fs_sensor(String mount_point) throws HException
	{
		Sampler s = null;
		String sampler_name;
		if (mount_point.charAt(0) == '+')
		{
			sampler_name = "asm_diskgroups";
		}
		else
		{
			sampler_name = "filesystems";
		}

		s = (Sampler) BgJobs.getJob(sampler_name);

		if (s != null)
		{
			@SuppressWarnings("unchecked")
			Map<String, FilesystemResult> results = (Map<String, FilesystemResult>) s.getLastResult();
			if (results != null)
			{
				FilesystemResult archive_dest_result = results.get(mount_point);
				if (archive_dest_result != null)
				{
					return archive_dest_result;
				}
				else
				{
					// No hay datos del FS especificado en config.get_archive_dest()
					L.error("No hay resultados para el filesystem [{}] en el sampler [{}]", mount_point, sampler_name);
					throw new HException("No hay resultados para el filesystem [" + mount_point + "] en el sampler [" + sampler_name + "]");
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
			L.error("El sampler [{}] no se encuentra operativo. No se pueden obtener datos de los filesystems", sampler_name);
			throw new HException("El sampler de filesystems no se encuentra disponible");
		}

	}

}
