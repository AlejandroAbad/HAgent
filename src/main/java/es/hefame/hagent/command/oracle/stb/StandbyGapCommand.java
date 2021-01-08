package es.hefame.hagent.command.oracle.stb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.oracle.stb.result.StandbyGapResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.oracle.StandbyGapConfigData;
import es.hefame.hcore.oracle.DBConnection;

public class StandbyGapCommand implements Command
{

	private static Logger		L		= LogManager.getLogger();
	private static final Marker	MARKER	= MarkerManager.getMarker("STANDBY_GAP_CMD");

	public StandbyGapCommand()
	{
		// TODO Auto-generated constructor stub
	}

	public StandbyGapResult operate() throws HException
	{
		StandbyGapConfigData config = (StandbyGapConfigData) CONF.checker.getMonitorizedElementByName("standby_gap");

		// CERRAMOS CONEXION PREVIA A LA BBDD
		// TODO: Hay que permitir multiconexion en el driver
		try
		{
			Connection closeDB = DBConnection.get();
			DBConnection.clearResources(closeDB);
		}
		catch (Exception ignore)
		{
		}

		StandbyGapResult result = new StandbyGapResult();

		// CONEXION A LA PRIMARY
		Connection primaryDB = null;
		Statement primarySTMT = null;
		ResultSet primaryRS = null;
		try
		{
			L.info(MARKER, "Consultando la PRIMARY database [{}][{}]", config.getPrimaryUser(), config.getPrimaryTns());
			DBConnection.setConnectionParameters(config.getPrimaryTns(), config.getPrimaryUser(), config.getPrimaryPassword());
			primaryDB = DBConnection.get();
			primarySTMT = primaryDB.createStatement();
			primaryRS = primarySTMT.executeQuery("SELECT THREAD# AS THREAD, MAX(SEQUENCE#) AS SEQ FROM V$ARCHIVED_LOG GROUP BY THREAD#");

			while (primaryRS.next())
			{
				int thread = primaryRS.getInt("THREAD");
				int seq = primaryRS.getInt("SEQ");
				result.addSequence(thread, seq, false);

				L.info(MARKER, "PRIMARY: La maxima seq# para el thread [{}] es [{}]", thread, seq);
			}
		}
		catch (SQLException e)
		{
			L.error("Ocurrio un error mientras se consultaba la SEQ# de la PRIMARY");
			L.catching(e);
		}
		finally
		{
			DBConnection.clearResources(primaryRS, primarySTMT, primaryDB);
		}

		// CONEXION A LA STB
		Connection stbDB = null;
		Statement stbSTMT = null;
		ResultSet stbRS = null;
		try
		{
			L.info(MARKER, "Consultando la STB database [{}][{}]", config.getStbUser(), config.getStbTns());
			DBConnection.setConnectionParameters(config.getStbTns(), config.getStbUser(), config.getStbPassword());
			stbDB = DBConnection.get();
			stbSTMT = stbDB.createStatement();
			stbRS = stbSTMT.executeQuery("SELECT THREAD# AS THREAD, MAX(SEQUENCE#) AS SEQ FROM V$ARCHIVED_LOG GROUP BY THREAD#");
			while (stbRS.next())
			{
				int thread = stbRS.getInt("THREAD");
				int seq = stbRS.getInt("SEQ");
				result.addSequence(thread, seq, true);

				L.info(MARKER, "STANDBY: La maxima seq# para el thread [{}] es [{}]", thread, seq);
			}
		}
		catch (SQLException e)
		{
			L.error("Ocurrio un error mientras se consultaba la SEQ# de la STB");
			L.catching(e);
		}
		finally
		{
			DBConnection.clearResources(stbRS, stbSTMT, stbDB);
		}

		return result.closeSensor();
	}

}
