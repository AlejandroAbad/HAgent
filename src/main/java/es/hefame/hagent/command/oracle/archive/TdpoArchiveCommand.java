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
import es.hefame.hagent.configuration.monitorized_element.oracle.archivelog.TdpoArchiveConfigData;

public class TdpoArchiveCommand extends ArchiveCommand {
	private static Logger L = LogManager.getLogger();
	private static final Marker ARCHIVE_CMD_MARKER = MarkerManager.getMarker("ARCHIVE_CMD");

	private TdpoArchiveConfigData config = null;

	public TdpoArchiveCommand(ArchivelogConfigData config_data) throws HException {
		if (config_data instanceof TdpoArchiveConfigData) {
			this.config = (TdpoArchiveConfigData) config_data;
		} else {
			L.error(ARCHIVE_CMD_MARKER,
					"No se puede crear el comando [{}] porque la configuracion es incompatible [{}]",
					this.getClass().getSimpleName(), config_data.getClass().getSimpleName());
			throw new HException("Comando incompatible");
		}
	}

	@Override
	public OsCommandResult operate() throws HException {
		StringBuilder std_input = new StringBuilder();

		std_input.append("run {");
		std_input.append("	allocate channel sbt_1 type sbt_tape parms '").append(config.get_extra_env())
				.append("ENV=(TDPO_OPTFILE=").append(config.get_tdpo_optfile()).append(")';");
		std_input.append("	backup archivelog all;");
		std_input.append("	release channel sbt_1;");
		std_input.append("}");
		std_input.append("crosscheck archivelog all;");
		std_input.append("delete noprompt archivelog all backed up 1 times to sbt_tape;");


		try {
			String oracleHome = config.get_oracle_home();
			String rmanBinScript = "rman target /";
			if (oracleHome.length() > 0) {
				rmanBinScript = "env ORACLE_HOME=" + oracleHome + " ORACLE_SID=" + config.get_db_name() + " " + oracleHome + "/bin/rman target /";
			}

			OsCommandExecutor comm = new OsCommandExecutor(ARCHIVE_CMD_MARKER, "su", "-", config.get_user(), "-c",
					rmanBinScript);
			OsCommandResult result = comm.run(std_input.toString().getBytes());
			L.debug(ARCHIVE_CMD_MARKER, "El resultado de la operacion es: [{}]", result.toString());
			return result;
		} catch (IOException e) {
			L.error(ARCHIVE_CMD_MARKER, "Ocurrio una excepcion al ejecutar el comando");
			L.catching(e);
			throw new HException("Ocurrio una excepcion al ejecutar el comando", e);
		}

	}

}
