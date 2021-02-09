package es.hefame.hagent;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.bg.BgJobs;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.controller.QuickTestHandler;
import es.hefame.hagent.controller.agent.AgentConfigurationHandler;
import es.hefame.hagent.controller.agent.AgentInfoHandler;
import es.hefame.hagent.controller.agent.AgentLog4jHandler;
import es.hefame.hagent.controller.agent.AgentRegisterHandler;
import es.hefame.hagent.controller.agent.AgentRestartHandler;
import es.hefame.hagent.controller.agent.AlertChannelTestHandler;
import es.hefame.hagent.controller.alarm.AlarmsHandler;
import es.hefame.hagent.controller.prtg.PrtgCheckersHandler;
import es.hefame.hagent.controller.prtg.PrtgDiskpathsHandler;
import es.hefame.hagent.controller.prtg.PrtgFilesystemsHandler;
import es.hefame.hagent.controller.prtg.PrtgInterfacesHandler;
import es.hefame.hagent.controller.prtg.PrtgJVMHandler;
import es.hefame.hagent.controller.prtg.PrtgMemoryHandler;
import es.hefame.hagent.controller.prtg.PrtgOGridResourcesHandler;
import es.hefame.hagent.controller.prtg.PrtgPingHandler;
import es.hefame.hagent.controller.prtg.PrtgProcessListHandler;
import es.hefame.hagent.controller.prtg.PrtgProcessorHandler;
import es.hefame.hagent.controller.prtg.apache.PrtgApacheServerStatusHandler;
import es.hefame.hagent.controller.prtg.oracle.PrtgStandbyGapHandler;
import es.hefame.hagent.controller.prtg.proyman.ProymanImpresoraHandler;
import es.hefame.hagent.controller.prtg.sap.PrtgSapProcessesHandler;
import es.hefame.hagent.controller.report.ReportOsUpdatesHandler;
import es.hefame.hagent.controller.report.proyman.ProymanIsap3060Handler;
import es.hefame.hagent.util.hook.ShutdownHook;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.server.HttpService;

public class HAgent {
	private static Logger L = LogManager.getLogger();

	public static void main(String... args) {

		String version = AgentInfo.get_version();
		String build = AgentInfo.get_build();
		String compiledDate = AgentInfo.get_built_date();
		L.info("Starting HAgent API {}.{} ({})", version, build, compiledDate);

		try {

			CONF.load();
			BgJobs.launch();

			int port = CONF.agent.port;
			int max_connections = 10;

			Map<String, HttpController> routes = new HashMap<String, HttpController>();
			routes.put("/test", new QuickTestHandler());

			// PRTG
			routes.put("/prtg/jvm", new PrtgJVMHandler());
			routes.put("/prtg/processor", new PrtgProcessorHandler());
			routes.put("/prtg/memory", new PrtgMemoryHandler());
			routes.put("/prtg/filesystems", new PrtgFilesystemsHandler());
			routes.put("/prtg/interfaces", new PrtgInterfacesHandler());
			routes.put("/prtg/diskpaths", new PrtgDiskpathsHandler());
			routes.put("/prtg/checkers", new PrtgCheckersHandler());
			routes.put("/prtg/ping", new PrtgPingHandler());
			routes.put("/prtg/processes", new PrtgProcessListHandler());
			routes.put("/prtg/sap/processes", new PrtgSapProcessesHandler());
			routes.put("/prtg/cluster", new PrtgOGridResourcesHandler()); // DEPRECATED - Usar /prtg/oracle/clusterware
			routes.put("/prtg/oracle/clusterware", new PrtgOGridResourcesHandler());
			routes.put("/prtg/oracle/standby_gap", new PrtgStandbyGapHandler());
			routes.put("/prtg/proyman/impresoras", new ProymanImpresoraHandler());
			routes.put("/prtg/apache/server-status", new PrtgApacheServerStatusHandler());

			routes.put("/report/proyman/isap3060", new ProymanIsap3060Handler());
			routes.put("/report/os_updates", new ReportOsUpdatesHandler());

			routes.put("/agent/info", new AgentInfoHandler());
			routes.put("/agent/log4j", new AgentLog4jHandler());
			routes.put("/agent/config", new AgentConfigurationHandler());
			routes.put("/agent/restart", new AgentRestartHandler());
			routes.put("/agent/register", new AgentRegisterHandler());
			routes.put("/agent/channeltest", new AlertChannelTestHandler());

			routes.put("/alarms", new AlarmsHandler());

			HttpService server = new HttpService(port, max_connections, routes);

			ShutdownHook shutdown_hook = new ShutdownHook(server);
			L.trace("Setting up ShutdownHook [{}]", shutdown_hook.getClass().getName());
			Runtime.getRuntime().addShutdownHook(new ShutdownHook(server));

			server.start();
		} catch (Exception e) {
			L.catching(e);
			L.fatal("Aborting execution with exit code {}", 2);
			System.exit(2);
		}

	}

}
