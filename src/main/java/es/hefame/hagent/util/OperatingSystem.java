package es.hefame.hagent.util;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.os.level.AIXOsLevelCommand;
import es.hefame.hagent.command.os.level.LNXOsLevelCommand;
import es.hefame.hagent.command.os.level.ORALNXOsLevelCommand;
import es.hefame.hagent.command.os.level.OsLevelCommand;
import es.hefame.hagent.command.os.level.WINOsLevelCommand;
import es.hefame.hagent.command.os.level.result.AIXOsLevelResult;
import es.hefame.hagent.command.os.level.result.LNXOsLevelResult;
import es.hefame.hagent.command.os.level.result.ORALNXOsLevelResult;
import es.hefame.hagent.command.os.level.result.OsLevelResult;
import es.hefame.hagent.command.os.level.result.WINOsLevelResult;

public enum OperatingSystem implements JsonEncodable
{

	UNKNOWN("Unknown OS", "UNK"), WIN("Windows", "WIN"), AIX("AIX", "AIX"), LINUX("SUSE Linux", "LNX"), ORALINUX("Oracle Linux", "ORALNX");

	private static Logger			L		= LogManager.getLogger();
	private static final Marker		M		= MarkerManager.getMarker("OSLEVEL_CMD");

	private static OperatingSystem	OS;
	public final String				name;
	public final String				code;
	public final String				arch	= System.getProperty("os.arch");
	private OsLevelResult			os_level;

	private OperatingSystem(String name, String code)
	{
		this.name = name;
		this.code = code;
		this.os_level = null;
	}

	public static void determine_os()
	{
		L.info(M, "Determinando el sistema operativo");

		String os_name = System.getProperty("os.name").toLowerCase();
		L.debug(M, "Valor de la propiedad 'os.name' es [" + os_name + "]");

		if (os_name.startsWith("windows"))
		{
			OS = OperatingSystem.WIN;

			try
			{
				WINOsLevelCommand op = (WINOsLevelCommand) CommandFactory.new_command(OsLevelCommand.class);
				OS.os_level = (WINOsLevelResult) op.operate();
			}
			catch (HException e)
			{
				OS.os_level = new WINOsLevelResult();
			}

		}
		else if (os_name.startsWith("linux"))
		{
			OsCommandExecutor c = new OsCommandExecutor(M, "uname", "-r");
			try
			{
				OsCommandResult osname = c.run();
				String os_release = new String(osname.get_stdout()).trim();

				if (os_release.indexOf("uek") == -1)
				{
					L.debug(M, "No se encuentra el patron 'uek' en la salida de 'uname -r'. Asumimos SUSE Linux");
					OS = OperatingSystem.LINUX;

					try
					{
						LNXOsLevelCommand op = (LNXOsLevelCommand) CommandFactory.new_command(OsLevelCommand.class);
						OS.os_level = op.operate();
					}
					catch (HException e)
					{
						OS.os_level = new LNXOsLevelResult();
					}

				}
				else
				{
					L.debug(M, "Se encuentra el patron 'uek' en la salida de 'uname -r'. Asumimos ORACLE Linux");
					OS = OperatingSystem.ORALINUX;

					try
					{
						ORALNXOsLevelCommand op = (ORALNXOsLevelCommand) CommandFactory.new_command(OsLevelCommand.class);
						OS.os_level = op.operate();
					}
					catch (HException e)
					{
						OS.os_level = new ORALNXOsLevelResult();
					}
				}
			}
			catch (IOException e)
			{
				L.catching(e);
				OS = OperatingSystem.LINUX;
			}

		}
		else if (os_name.startsWith("aix"))
		{

			OS = OperatingSystem.AIX;

			try
			{
				AIXOsLevelCommand op = (AIXOsLevelCommand) CommandFactory.new_command(OsLevelCommand.class);
				op.operate();
				OS.os_level = op.operate();
			}
			catch (HException e)
			{
				OS.os_level = new AIXOsLevelResult();
			}

		}
		else
		{
			OS = OperatingSystem.UNKNOWN;
			L.fatal(M, "No se pudo identificar el sustema operativo [" + os_name + "]");
			System.exit(1);
		}

		L.info(M, "Sistema operativo identificado como [" + OS.name + "]");
		L.info(M, "Version del SO [{}]", OS.os_level.get_version());
	}

	public static OperatingSystem get_os()
	{
		if (OS == null) determine_os();
		return OS;
	}

	public OsLevelResult get_os_level()
	{
		return this.os_level;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("name", name);
		root.put("architecture", arch);
		root.put("code", code);
		if (this.os_level != null)
		{
			root.put("level", this.os_level.jsonEncode());
		}
		return root;
	}
}
