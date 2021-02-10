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

	public TdpoArchiveCommand(ArchivelogConfigData configData) throws HException {
		if (configData instanceof TdpoArchiveConfigData) {
			this.config = (TdpoArchiveConfigData) configData;
		} else {
			L.error(ARCHIVE_CMD_MARKER,
					"No se puede crear el comando [{}] porque la configuracion es incompatible [{}]",
					this.getClass().getSimpleName(), configData.getClass().getSimpleName());
			throw new HException("Comando incompatible");
		}
	}

	@Override
	public OsCommandResult operate() throws HException {
		StringBuilder stdInput = new StringBuilder();

		stdInput.append("run {");
		stdInput.append(" allocate channel sbt_1 type sbt_tape parms '").append(config.getExtraEnv()).append("ENV=(TDPO_OPTFILE=").append(config.getTdpoOptfile()).append(")';");
		stdInput.append(" backup archivelog all;");
		stdInput.append(" release channel sbt_1;");
		stdInput.append("}");
		stdInput.append("crosscheck archivelog all;");
		stdInput.append("delete noprompt archivelog all backed up 1 times to sbt_tape;");


		try {
			String oracleHome = config.getOracleHome();
			String rmanBinScript = "rman target /";
			if (oracleHome.length() > 0) {
				rmanBinScript = "env ORACLE_HOME=" + oracleHome + " ORACLE_SID=" + config.getDbName() + " " + oracleHome + "/bin/rman target /";
			}

			OsCommandExecutor comm = new OsCommandExecutor(ARCHIVE_CMD_MARKER, "su", "-", config.getUser(), "-c",
					rmanBinScript);
			OsCommandResult result = comm.run(stdInput.toString().getBytes());
			L.debug(ARCHIVE_CMD_MARKER, "El resultado de la operacion es: [{}]", () -> result.toString());
			return result;
		} catch (IOException e) {
			L.error(ARCHIVE_CMD_MARKER, "Ocurrio una excepcion al ejecutar el comando");
			L.catching(e);
			throw new HException("Ocurrio una excepcion al ejecutar el comando", e);
		}

	}

}
