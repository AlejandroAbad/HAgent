package es.hefame.hagent.command.oracle.asmdiskgroups;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.oracle.asmdiskgroups.result.AsmDiskgroupResult;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.monitorized_element.oracle.AsmDiskgroupsConfigData;
import es.hefame.hcore.oracle.DBConnection;

public class AsmDiskgroupsCommand implements Command
{
	private static Logger L = LogManager.getLogger();

	public Map<String, AsmDiskgroupResult> operate() throws HException
	{
		Map<String, AsmDiskgroupResult> results = new HashMap<String, AsmDiskgroupResult>();
		AsmDiskgroupsConfigData config = (AsmDiskgroupsConfigData) CONF.checker.getMonitorizedElementByName("asm_diskgroups");
		String tns = config.get_tns();
		String user = config.get_user() + " AS SYSASM";
		String pass = config.get_password();

		DBConnection.setConnectionParameters(tns, user, pass);
		Connection db = DBConnection.get();
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = db.createStatement();
			rs = stmt.executeQuery("SELECT NAME, STATE, TYPE, TOTAL_MB, REQUIRED_MIRROR_FREE_MB, USABLE_FILE_MB, OFFLINE_DISKS FROM V$ASM_DISKGROUP_STAT");
			while (rs.next())
			{
				String name = rs.getString("NAME");
				String state = rs.getString("STATE");
				String redundancy = rs.getString("TYPE");
				int total_mb = rs.getInt("TOTAL_MB");
				int required_mirror_free_mb = rs.getInt("REQUIRED_MIRROR_FREE_MB");
				int usable_file_mb = rs.getInt("USABLE_FILE_MB");
				int offline_disks = rs.getInt("OFFLINE_DISKS");

				AsmDiskgroupResult result = new AsmDiskgroupResult(name, state, redundancy, total_mb, required_mirror_free_mb, usable_file_mb, offline_disks);
				results.put(result.get_mount_point(), result);
			}
		}
		catch (SQLException e)
		{
			L.error("Ocurrio un error mientras se parseaba el diskgroup ASM");
			L.catching(e);
		}
		finally
		{
			DBConnection.clearResources(rs, stmt);
		}

		return results;
	}
}
