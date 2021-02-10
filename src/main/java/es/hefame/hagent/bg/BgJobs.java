package es.hefame.hagent.bg;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.checker.common.AlertlogChecker;
import es.hefame.hagent.bg.checker.common.ErrptChecker;
import es.hefame.hagent.bg.checker.oracle.OracleArchivelogChecker;
import es.hefame.hagent.bg.checker.oracle.OracleAlertlogChecker;
import es.hefame.hagent.bg.checker.proyman.ProymanImpresoraChecker;
import es.hefame.hagent.bg.checker.proyman.ProymanIsap3060Checker;
import es.hefame.hagent.bg.sampler.Sampler;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.filesystems.FilesystemsCommand;
import es.hefame.hagent.command.interfaces.InterfacesCommand;
import es.hefame.hagent.command.oracle.asmdiskgroups.AsmDiskgroupsCommand;
import es.hefame.hagent.command.processor.ProcessorCommand;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfigData;
import es.hefame.hagent.configuration.monitorized_element.common.AlertlogConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.OracleAlertlogConfigData;
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.ArchivelogConfigData;
import es.hefame.hagent.configuration.monitorized_element.proyman.ProymanImpresoraConfigData;
import es.hefame.hagent.configuration.monitorized_element.proyman.ProymanIsap3060ConfigData;
import es.hefame.hagent.util.exception.CommandNotSupportedException;

public class BgJobs
{
	private static Logger							L							= LogManager.getLogger();
	private static Map<String, RecurringOperation>	background_jobs				= new HashMap<String, RecurringOperation>();

	public static final int							FILESYSTEM_SAMPLERS_DELAY	= 1 * 60 * 1000;

	public static void launch()
	{
		// PROCESSOR
		try
		{
			Sampler cpu = new Sampler(CommandFactory.new_command(ProcessorCommand.class), CONF.prtg.processor.sample_time);
			addJob("processor", cpu);
			cpu.start();
		}
		catch (HException e)
		{
			L.error("Ocurrio un error al arrancar el Sampler de procesador");
			L.catching(e);
		}

		// INTERFACES
		try
		{
			Sampler interfaces = new Sampler(CommandFactory.new_command(InterfacesCommand.class));
			addJob("interfaces", interfaces);
			interfaces.start();
		}
		catch (CommandNotSupportedException e)
		{
			L.error("Ocurrio un error al arrancar el Sampler de las interfaces");
			L.catching(e);
		}

		// FILESYSTEMS
		try
		{
			Sampler filesystems = new Sampler(CommandFactory.new_command(FilesystemsCommand.class), FILESYSTEM_SAMPLERS_DELAY);
			addJob("filesystems", filesystems);
			filesystems.start();
		}
		catch (CommandNotSupportedException e)
		{
			L.error("Ocurrio un error al arrancar el Sampler de los filesystems");
			L.catching(e);
		}

		// DISKGROUPS ASM
		if (CONF.checker.getMonitorizedElementByName("asm_diskgroups") != null)
		{
			Sampler asm = new Sampler(new AsmDiskgroupsCommand(), FILESYSTEM_SAMPLERS_DELAY);
			addJob("asm_diskgroups", asm);
			asm.start();
		}

		// ORACLE ALERTLOGS
		for (MonitorizedElementConfigData alerter : CONF.checker.get_element_of_type("oracle_alertlog"))
		{
			OracleAlertlogConfigData oracle_alertlog = (OracleAlertlogConfigData) alerter;
			String db_name = oracle_alertlog.get_db_name();

			OracleAlertlogChecker alertlog = new OracleAlertlogChecker("oracle_alertlog_" + db_name.toLowerCase(), db_name);
			addJob(alertlog.getCheckerName(), alertlog);
			alertlog.start();
		}

		// ALERTLOGS
		for (MonitorizedElementConfigData alerter : CONF.checker.get_element_of_type("alertlog"))
		{
			AlertlogConfigData alertlog_config = (AlertlogConfigData) alerter;
			String log_name = alertlog_config.get_log_name();

			AlertlogChecker alertlog_checker = new AlertlogChecker(log_name);
			addJob(alertlog_checker.getCheckerName(), alertlog_checker);
			alertlog_checker.start();
		}

		// ERRPT
		if (CONF.checker.getMonitorizedElementByName("errpt") != null)
		{
			ErrptChecker errpt = new ErrptChecker();
			addJob("errpt", errpt);
			errpt.start();
		}

		// ARCHIVELOGS
		for (MonitorizedElementConfigData alerter : CONF.checker.get_element_of_type("archivelog"))
		{
			ArchivelogConfigData archive_config = (ArchivelogConfigData) alerter;
			String db_name = archive_config.getDbName();

			OracleArchivelogChecker archivelog_checker = new OracleArchivelogChecker(db_name);
			addJob(archivelog_checker.getCheckerName(), archivelog_checker);
			archivelog_checker.start();
		}

		// PROYMAN IMPRESORA
		ProymanImpresoraConfigData proymanImpresoraConfig = (ProymanImpresoraConfigData) CONF.checker.getMonitorizedElementByName("proyman_impresora");
		if (proymanImpresoraConfig != null)
		{
			ProymanImpresoraChecker pi_checker = new ProymanImpresoraChecker(proymanImpresoraConfig);
			addJob(pi_checker.getCheckerName(), pi_checker);
			pi_checker.start();
		}

		// PROYMAN ISAP3060
		ProymanIsap3060ConfigData proymanIsap3060Config = (ProymanIsap3060ConfigData) CONF.checker.getMonitorizedElementByName("proyman_isap3060");
		if (proymanIsap3060Config != null)
		{
			ProymanIsap3060Checker isap3060checker = new ProymanIsap3060Checker(proymanIsap3060Config);
			addJob(isap3060checker.getCheckerName(), isap3060checker);
			isap3060checker.start();
		}

	}

	public static RecurringOperation getJob(String name)
	{
		return background_jobs.get(name);
	}

	public static void addJob(String name, RecurringOperation job)
	{
		background_jobs.put(name, job);
	}

}
